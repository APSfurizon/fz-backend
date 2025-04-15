package net.furizon.backend.feature.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.feature.user.UserSession;

import java.util.List;

@Data
public class SessionListResponse {
    @NotNull private final List<UserSession> sessions;
}
