package net.furizon.backend.feature.authentication.dto;

import lombok.Data;

@Data
public class RegisterUserResponse {
    public static final RegisterUserResponse SUCCESS = new RegisterUserResponse(true);

    private final boolean success;
}
