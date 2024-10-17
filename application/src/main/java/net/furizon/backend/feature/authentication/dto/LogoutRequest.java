package net.furizon.backend.feature.authentication.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private final long userId;
}
