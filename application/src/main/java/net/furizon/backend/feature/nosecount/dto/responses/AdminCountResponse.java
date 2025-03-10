package net.furizon.backend.feature.nosecount.dto.responses;

import lombok.Data;
import net.furizon.backend.feature.nosecount.dto.AdminCountRole;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class AdminCountResponse {
    @NotNull private final List<AdminCountRole> roles;
}
