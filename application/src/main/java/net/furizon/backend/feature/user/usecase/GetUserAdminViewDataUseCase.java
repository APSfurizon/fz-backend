package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.badge.usecase.GetFullInfoBadgeUseCase;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.response.ExchangeConfirmationStatusResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.finder.ExchangeConfirmationFinder;
import net.furizon.backend.feature.room.usecase.GetExchangeConfirmationStatusInfoUseCase;
import net.furizon.backend.feature.room.usecase.GetRoomInfoUseCase;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.UserAdminViewData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUserAdminViewDataUseCase {
    @NotNull private final UseCaseExecutor executor;

    @NotNull private final SessionAuthenticationManager authenticationFinder;

    @NotNull private final UserFinder userFinder;
    @NotNull private final EventFinder eventFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final PersonalInfoFinder personalInfoFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final ExchangeConfirmationFinder exchangeConfirmationFinder;
    @NotNull private final TranslationService translationService;

    public @NotNull UserAdminViewData execute(
            @NotNull FurizonUser user,
            long userId,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {
        // Fetch userData
        PersonalUserInformation privateInfo = personalInfoFinder.findByUserId(userId);
        Authentication auth = authenticationFinder.findAuthenticationByUserId(userId);
        User jooqUser = userFinder.findById(userId);
        if (privateInfo == null || auth == null || jooqUser == null) {
            throw new ApiException(translationService.error("user.not_found"),
                GeneralResponseCodes.USER_NOT_FOUND);
        }

        // Fetch all user's cards
        List<MembershipCard> cards = membershipCardFinder.getCardsOfUser(userId);

        // Fetch past orders
        List<Order> orders = orderFinder.findAllOrdersOfUser(userId, pretixInformation);

        // Fetch current room data
        RoomInfoResponse roomInfo = executor.execute(
                GetRoomInfoUseCase.class,
                new GetRoomInfoUseCase.Input(
                        userId,
                        event,
                        pretixInformation,
                        true,
                        true
                )
        );
        // Fetch running exchanges
        List<ExchangeConfirmationStatus> exchangesIds = exchangeConfirmationFinder.getAllExchangesOfUserInEvent(
                userId,
                event
        );
        List<ExchangeConfirmationStatusResponse> exchanges = exchangesIds.stream().map(e -> {
            long exchangeId = e.getExchangeId();
            return executor.execute(
                    GetExchangeConfirmationStatusInfoUseCase.class,
                    new GetExchangeConfirmationStatusInfoUseCase.Input(
                            user,
                            exchangeId,
                            pretixInformation,
                            true
                    )
            );
        }).toList();
        //Fetch previous rooms
        List<RoomInfoResponse> prevRooms = eventFinder.getAllEvents().stream().map(e -> {
            if (!e.equals(event)) {
                RoomInfoResponse resp = executor.execute(
                        GetRoomInfoUseCase.class,
                        new GetRoomInfoUseCase.Input(
                                userId,
                                e,
                                pretixInformation,
                                true,
                                false
                        )
                );
                if (!resp.isHasOrder()) {
                    return null;
                }
                resp.setCanCreateRoom(false);
                resp.setBuyOrUpgradeRoomSupported(false);
                resp.setCanBuyOrUpgradeRoom(false);
                resp.setCanExchange(false);
                RoomInfo room = resp.getCurrentRoomInfo();
                if (room != null) {
                    room.setConfirmationSupported(false);
                    room.setCanConfirm(false);
                    room.setUnconfirmationSupported(false);
                    room.setCanUnconfirm(false);
                    room.setCanInvite(false);
                } else {
                    if (resp.getInvitations().isEmpty()) {
                        return null;
                    }
                }
                return resp;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).toList();

        // Fetch badge + fursuit data
        FullInfoBadgeResponse badgeData = executor.execute(
                GetFullInfoBadgeUseCase.class,
                new GetFullInfoBadgeUseCase.Input(
                        userId,
                        pretixInformation
                )
        );

        // Fetch roles and permissions
        List<Role> roles = permissionFinder.getRolesFromUserId(userId);
        Set<Permission> permissions = permissionFinder.getUserPermissions(userId);

        return UserAdminViewData.builder()
                .personalInfo(privateInfo)
                .membershipCards(cards)
                .orders(orders)
                .email(auth.getEmail())
                .banned(auth.isDisabled())
                .currentRoomdata(roomInfo)
                .exchanges(exchanges)
                .otherRooms(prevRooms)
                .showInNousecount(jooqUser.isShowInNoseCount())
                .badgeData(badgeData)
                .roles(roles)
                .permissions(permissions)
            .build();
    }
}
