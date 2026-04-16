package net.furizon.backend.feature.pretix.objects.checkins.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.usecase.export.GenerateBadgesHtmlUseCase;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.checkins.PortaBadgeLevel;
import net.furizon.backend.feature.pretix.objects.checkins.action.redeemCheckin.PretixRedeemCheckinAction;
import net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets.GadgetManager;
import net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets.GadgetPermissionService;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.RedeemCheckinResponse;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.CheckinResponse;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedeemCheckinUseCase implements UseCase<RedeemCheckinUseCase.Input, CheckinResponse> {
    @NotNull
    private final GeneralChecks checks;
    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final FursuitFinder fursuitFinder;
    @NotNull
    private final MembershipCardFinder membershipCardFinder;
    @NotNull
    private final PersonalInfoFinder personalInfoFinder;
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final RoomFinder roomFinder;

    @NotNull
    private final RoomLogic roomLogic;

    @NotNull
    private final PretixRedeemCheckinAction redeemCheckinAction;

    @NotNull
    private final TranslationService translationService;

    @NotNull
    private final GadgetPermissionService gadgetPermissionService;

    @Override
    public @NotNull CheckinResponse executor(@NotNull RedeemCheckinUseCase.Input input) {
        log.info("User {} is checking in secret {}", input.user.getUserId(), input.secret);

        Order o = checks.getOrderAndAssertItExists(input.secret, input.event, input.pretixService);
        long orderOwner = checks.assertUserFound(o.getOrderOwnerUserId());
        var permissions = permissionFinder.getUserPermissions(orderOwner);

        var pui = checks.assertUserFound(personalInfoFinder.findByUserId(orderOwner));
        var user = checks.assertUserFound(userFinder.getDisplayUser(orderOwner, input.event));
        var fursuits = fursuitFinder.getFursuitsOfUserBroughtToEvent(orderOwner, input.event);
        var membershipCards = membershipCardFinder.getCardsOfUserForEvent(orderOwner, input.event);
        boolean isFursuiter = !fursuits.isEmpty();
        boolean isDailyTicket = o.isDaily();
        var lanyardType = GenerateBadgesHtmlUseCase.getBadgeLevel(o.getSponsorship(), isDailyTicket, permissions);
        var portaBadgeType = getPortaBadgeLevel(permissions, isFursuiter, isDailyTicket);
        var roomInfo = roomFinder.getRoomInfoForUser(orderOwner, input.event, input.pretixService, roomLogic);
        boolean isStaffer = permissions.contains(Permission.JUNIOR_STAFF)
                         || permissions.contains(Permission.SECURITY_STAFF)
                         || permissions.contains(Permission.MAIN_STAFF);
        var gadgets = new GadgetManager(o.getGadgets());
        permissions.forEach(p -> gadgets.addAll(gadgetPermissionService.getGadgetsForPermission(p)));

        String nonce = UUID.randomUUID().toString();

        RedeemCheckinResponse checkinResponse = redeemCheckinAction.invoke(
                input.secret,
                input.event.getOrganizerAndEventPair().getOrganizer(),
                nonce,
                input.checkinListIds
        );
        if (checkinResponse == null) {
            log.error("CheckinResponse is null!");
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    translationService.error("checkin.redeem.not-found"),
                    GeneralResponseCodes.GENERIC_ERROR
            );
        }
        var reason = checkinResponse.getReason();
        String localizedErrorReason = reason == null
                                    ? null
                                    : reason.getErrorMessage().localizeError(translationService);

        return CheckinResponse.builder()
                .checkinNonce(nonce)
                .status(checkinResponse.getStatus())
                .localizedErrorReason(localizedErrorReason)
                .optionalErrorMessage(checkinResponse.getMessage())
                .user(user)
                .orderCode(o.getCode())
                .orderSerial(o.getOrderSerialInEvent())
                .cardsForEvent(membershipCards)
                .fursuits(fursuits)
                .hasFursuitBadge(isFursuiter)
                .isDailyTicket(isDailyTicket)
                .isStaffer(isStaffer)
                .sponsorship(o.getSponsorship())
                .lanyardType(lanyardType)
                .portaBadgeType(portaBadgeType)
                .roomInfo(roomInfo)
                .gadgets(gadgets.getGadgets())
                .firstName(pui.getFirstName())
                .lastName(pui.getLastName())
                .birthCity(pui.getBirthCity())
                .birthRegion(pui.getBirthRegion())
                .birthCountry(pui.getBirthCountry())
                .birthday(pui.getBirthday())
                //.allergies(pui.getAllergies())
                .fiscalCode(pui.getFiscalCode())
                .shirtSize(pui.getShirtSize())
                .idType(pui.getIdType())
                .idNumber(pui.getIdNumber())
                .idIssuer(pui.getIdIssuer())
                .idExpiry(pui.getIdExpiry())
                .requiresAttention(checkinResponse.getRequireAttention())
                .customerNote(o.getUserComment())
                .internalNote(o.getInternalComment())
                .checkinTexts(checkinResponse.getCheckinTexts())
            .build();
    }

    public static @NotNull PortaBadgeLevel getPortaBadgeLevel(@NotNull Collection<Permission> permissions,
                                                              boolean isFursuiter,
                                                              boolean isDaily) {
        PortaBadgeLevel level = PortaBadgeLevel.NORMAL_ATTENDEES;
        //In your convention you may want to reimplement this
        if (isDaily) {
            level = PortaBadgeLevel.DAILY_ATTENDEES;
        }
        if (isFursuiter) {
            level = PortaBadgeLevel.FURSUITERS;
        }
        if (permissions.contains(Permission.JUNIOR_STAFF)) {
            level = PortaBadgeLevel.GENERAL_STAFF;
        }
        if (permissions.contains(Permission.SECURITY_STAFF) || permissions.contains(Permission.MAIN_STAFF)) {
            level = PortaBadgeLevel.MAIN_AND_SECURITY_STAFF;
        }
        return level;
    }

    public record Input(
            @NotNull List<Long> checkinListIds,
            @NotNull String secret,
            @NotNull Event event,
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixService
    ) {}
}
