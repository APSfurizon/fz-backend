package net.furizon.backend.feature.authentication.dto.requests;

import lombok.Data;

@Data
public class DestroySessionRequest {
    private String sessionId;
}
