package net.furizon.backend.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.room.dto.response.ExchangeConfirmationStatusResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class UserAdminViewData {
    //Info about the user
    @NotNull
    private final PersonalUserInformation personalInfo;

    //List of membership cards of user
    @NotNull
    private final List<MembershipCard> membershipCards;

    //Authentication data
    @NotNull
    private final String email;
    private final boolean banned;

    //Orders
    @NotNull
    private final List<Order> orders;

    //Room in current event
    @Nullable
    private final RoomInfoResponse currentRoomdata;
    @NotNull
    private final List<ExchangeConfirmationStatusResponse> exchanges;
    //Past rooms
    @NotNull
    private final List<RoomInfoResponse> otherRooms;

    //Badge + Fursuits + bringing in current event
    private final boolean showInNousecount;
    @NotNull
    private final FullInfoBadgeResponse badgeData;

    //Role list + permissions
    @NotNull
    private final List<Role> roles;
    @NotNull
    private final Set<Permission> permissions;
}
