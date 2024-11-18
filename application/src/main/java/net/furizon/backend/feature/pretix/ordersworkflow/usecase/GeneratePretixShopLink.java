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
import net.furizon.backend.infrastructure.pretix.autocart.AutocartAction;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.BOOL;
import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.VALUE;
import static net.furizon.backend.infrastructure.pretix.autocart.AutocartActionType.DROPDOWN;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePretixShopLink implements UseCase<GeneratePretixShopLink.Input, LinkResponse> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final PersonalInfoFinder personalInfoFinder;

    @Override
    public @NotNull LinkResponse executor(@NotNull GeneratePretixShopLink.Input input) {
        List<AutocartAction<?>> actions = new ArrayList<>();
        long userId = input.user.getUserId();
        String mail = input.user.getEmail();
        Event event = input.event;

        int ordersNo = orderFinder.countOrdersOfUserOnEvent(userId, event);
        if (ordersNo > 0) {
            throw new ApiException(
                    "You already made an order!",
                    OrderWorkflowErrorCode.ORDER_ALREADY_DONE.name()
            );
        }

        int membershipNo = membershipCardFinder.countCardsPerUserPerEvent(userId, event);
        PersonalUserInformation info = personalInfoFinder.findByUserId(userId);

        actions.add(new AutocartAction<>("id_email", mail, VALUE));
        if (info != null) {
            actions.add(new AutocartAction<>("id_name_parts_0", info.getFirstName(), VALUE));
            actions.add(new AutocartAction<>("id_name_parts_1", info.getLastName(), VALUE));
            actions.add(new AutocartAction<>("id_street", info.getResidenceAddress(), VALUE));
            actions.add(new AutocartAction<>("id_zipcode", info.getResidenceZipCode(), VALUE));
            actions.add(new AutocartAction<>("id_city", info.getResidenceZipCode(), VALUE));
            actions.add(new AutocartAction<>("id_country", info.getResidenceCountry(), DROPDOWN));
            String region = info.getResidenceRegion();
            if (region != null) {
                actions.add(new AutocartAction<>("id_state", info.getResidenceRegion(), DROPDOWN));
            }
            actions.add(new AutocartAction<>("id_$-attendee_name_parts_0", info.getFirstName(), VALUE));
            actions.add(new AutocartAction<>("id_$-attendee_name_parts_1", info.getLastName(), VALUE));
        }
        if (membershipNo == 0) {
            Set<Integer> mcIds = input.pretixService.getIdsForItemType(CacheItemTypes.MEMBERSHIP_CARDS);
            for (int membershipCardId : mcIds) {
                actions.add(new AutocartAction<>("cp_$_item_" + membershipCardId, true, BOOL));
            }
        }

        return null;
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event,
            @NotNull PretixInformation pretixService) {}
}