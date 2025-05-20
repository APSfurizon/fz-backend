package net.furizon.backend.feature.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapabilitiesResponse {
    private final boolean canUpgradeUser;
    private final boolean canBanUsers;
    private final boolean canManageMembershipCards;
    private final boolean canRefreshPretixCache;
    private final boolean canRemindOrderLinking;
    private final boolean canRemindBadgeUploads;
    private final boolean canViewUsers;
    private final boolean canExportHotelList;
    private final boolean canExportBadges;
    private final boolean canRemindRoomsNotFull;
}
