package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.infrastructure.configuration.MembershipConfig;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.autocart.AutocartAction;
import net.furizon.backend.infrastructure.pretix.autocart.AutocartLinkGenerator;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.BOOL;
import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.DROPDOWN;
import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.INPUT;
import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.TEXT_AREA;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePretixShopLink implements UseCase<GeneratePretixShopLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final PersonalInfoFinder personalInfoFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final AutocartLinkGenerator autocartLinkGenerator;
    @NotNull private final MembershipConfig membershipConfig;
    @NotNull private final PretixConfig pretixConfig;

    @Override
    public @NotNull LinkResponse executor(@NotNull GeneratePretixShopLink.Input input) {
        final List<AutocartAction<?>> actions = new ArrayList<>();
        final long userId = input.user.getUserId();
        final String mail = input.user.getEmail();
        final PretixInformation pretixService = input.pretixService;
        final Event event = pretixService.getCurrentEvent();

        // Public event orders check
        OffsetDateTime bookingStart = pretixConfig.getEvent().getPublicBookingStartTime();
        boolean earlyBook = permissionFinder.userHasPermission(userId, Permission.EARLY_BOOK);
        if (bookingStart != null && bookingStart.isAfter(OffsetDateTime.now()) && !earlyBook) {
            log.error("User requested a shop link before opening date!");
            throw new ApiException("Shop is not available yet!", OrderWorkflowErrorCode.SHOP_NOT_OPENED_YET);
        }

        // Orders no check
        int membershipNo = 1; //By deafault, we don't ask for a membership if there's an error
        int ordersNo = orderFinder.countOrdersOfUserOnEvent(userId, event);
        if (ordersNo > 0) {
            throw new ApiException(
                    "You already made an order!",
                    OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE
            );
        }

        membershipNo = membershipCardFinder.countCardsPerUserPerEvent(userId, event);

        PersonalUserInformation info = personalInfoFinder.findByUserId(userId);

        actions.add(new AutocartAction<>("id_email", mail, INPUT));
        actions.add(new AutocartAction<>("id_email_repeat", mail, INPUT));
        if (info != null) {
            validatePersonalInfo(info, event);
            actions.add(new AutocartAction<>("id_phone_0", info.getPrefixPhoneNumber(), DROPDOWN));
            actions.add(new AutocartAction<>("id_phone_1", info.getPhoneNumber(), INPUT));
            actions.add(new AutocartAction<>("id_name_parts_0", info.getFirstName(), INPUT));
            actions.add(new AutocartAction<>("id_name_parts_1", info.getLastName(), INPUT));
            actions.add(new AutocartAction<>("id_street", info.getResidenceAddress(), TEXT_AREA));
            actions.add(new AutocartAction<>("id_zipcode", info.getResidenceZipCode(), INPUT));
            actions.add(new AutocartAction<>("id_city", info.getResidenceCity(), INPUT));
            actions.add(new AutocartAction<>("id_country", info.getResidenceCountry(), DROPDOWN));
            String region = info.getResidenceRegion();
            if (region != null) {
                actions.add(new AutocartAction<>("id_state", info.getResidenceRegion(), DROPDOWN));
            }
            actions.add(new AutocartAction<>("id_$-attendee_name_parts_0", info.getFirstName(), INPUT));
            actions.add(new AutocartAction<>("id_$-attendee_name_parts_1", info.getLastName(), INPUT));
            actions.add(new AutocartAction<>("id_$-attendee_email", mail, INPUT));
            actions.add(new AutocartAction<>("id_$-street", info.getResidenceAddress(), TEXT_AREA));
            actions.add(new AutocartAction<>("id_$-zipcode", info.getResidenceZipCode(), INPUT));
            actions.add(new AutocartAction<>("id_$-city", info.getResidenceCity(), INPUT));
            actions.add(new AutocartAction<>("id_$-country", info.getResidenceCountry(), INPUT));
            if (region != null) {
                actions.add(new AutocartAction<>("id_$-state", info.getResidenceRegion(), DROPDOWN));
            }
        }
        if (membershipNo == 0) {
            Set<Long> mcIds = input.pretixService.getIdsForItemType(CacheItemTypes.MEMBERSHIP_CARDS);
            for (long membershipCardId : mcIds) {
                actions.add(new AutocartAction<>("cp_$_item_" + membershipCardId, true, BOOL));
            }
        }

        var generatedUrl = autocartLinkGenerator.generateUrl(actions);
        if (generatedUrl.isEmpty()) {
            throw new RuntimeException("Autocart link generation failed.");
        }
        return new LinkResponse(generatedUrl.get() + "&z=0"); //Random param to prevent typos from destroying payload
    }

    public void validatePersonalInfo(@NotNull PersonalUserInformation info, @NotNull Event event) {
        // User has marked his personal info as updated check
        if (info.getLastUpdatedEventId() != event.getId()) {
            throw new ApiException(
                "User has to first update his personal info or mark them as updated!",
                OrderWorkflowErrorCode.PERSONAL_USER_INFO_NOT_UPDATED
            );
        }

        // User has the correct age check
        LocalDate birthday = info.getBirthday();
        //If event doesn't have a start date set, we default to the current date
        LocalDate date = event.getDateFrom() == null ? LocalDate.now() : event.getDateFrom().toLocalDate();

        long age = ChronoUnit.YEARS.between(birthday, date);
        long minimumAge = membershipConfig.getMinimumAgeAtEventDate();
        if (age < minimumAge) {
            throw new ApiException(
                    "You must be at least " + minimumAge + " years old to attend the event!",
                    OrderWorkflowErrorCode.BELOW_MINIMUM_AGE
            );
        }

    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull PretixInformation pretixService) {}
}