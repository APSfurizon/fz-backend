package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ListingRolesResponse {
    @NotNull private final List<ListedRoleResponse> roles;
}
