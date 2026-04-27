package net.furizon.backend.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserAdminReducedViewData {
    //Info about the user
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @NotNull
    private final String sex;
    @Nullable
    private final String gender;
    @NotNull
    private final LocalDate birthday;
    @NotNull
    private final String phoneNumber;
    @NotNull
    private final String prefixPhoneNumber;
    @Nullable
    private final String allergies;
    @NotNull
    private final String telegramUsername;


    //List of membership cards of user
    @Nullable
    private final MembershipCard currentMembershipCard;

    //Authentication data
    @NotNull
    private final String email;

    //Orders
    @Nullable
    private final Order currentOrder;

    //Room in current event
    @Nullable
    private final RoomInfoResponse currentRoomdata;

    //Badge + Fursuits + bringing in current event
    @NotNull
    private final FullInfoBadgeResponse badgeData;

    //Role list + permissions
    @NotNull
    private final List<Role> roles;

    //Extra
    @NotNull
    private final List<UserAdminViewData.SponsorNamesForEvent> sponsorNames;

    public UserAdminReducedViewData(@NotNull UserAdminViewData userAdminViewData,
                                    @NotNull Event event,
                                    @NotNull MembershipYearUtils membershipYearUtils) {
        PersonalUserInformation personalInfo = userAdminViewData.getPersonalInfo();
        this.firstName = personalInfo.getFirstName();
        this.lastName = personalInfo.getLastName();
        this.sex = personalInfo.getSex();
        this.gender = personalInfo.getGender();
        this.birthday = personalInfo.getBirthday();
        this.phoneNumber = personalInfo.getPhoneNumber();
        this.prefixPhoneNumber = personalInfo.getPrefixPhoneNumber();
        this.allergies = personalInfo.getAllergies();
        this.telegramUsername = personalInfo.getTelegramUsername();

        this.email = userAdminViewData.getEmail();
        this.currentRoomdata = userAdminViewData.getCurrentRoomdata();
        this.badgeData = userAdminViewData.getBadgeData();
        this.roles = userAdminViewData.getRoles();
        this.sponsorNames = userAdminViewData.getSponsorNames();

        MembershipCard foundCard = null;
        short membershipYear = event.getMembershipYear(membershipYearUtils);
        List<MembershipCard> cards = userAdminViewData.getMembershipCards();
        for (MembershipCard card : cards) {
            if (membershipYear == card.getIssueYear()) {
                foundCard = card;
                break;
            }
        }
        this.currentMembershipCard = foundCard;

        Order foundOrder = null;
        long eventId = event.getId();
        List<Order> orders = userAdminViewData.getOrders();
        for (Order order : orders) {
            if (eventId == order.getEventId()) {
                foundOrder = order;
                break;
            }
        }
        this.currentOrder = foundOrder;
    }
}
