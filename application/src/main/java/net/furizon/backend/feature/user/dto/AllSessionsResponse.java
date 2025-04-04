package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.user.UserSession;

import java.util.List;

@Data
public class AllSessionsResponse {
    private List<UserSession> sessions;
}
