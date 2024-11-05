package net.furizon.backend.feature.authentication.dto;

import lombok.Data;

@Data
public class LogoutUserResponse {
    public static final LogoutUserResponse SUCCESS = new LogoutUserResponse(true);

    private final boolean success;
}
