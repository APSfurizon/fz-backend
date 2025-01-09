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
}
