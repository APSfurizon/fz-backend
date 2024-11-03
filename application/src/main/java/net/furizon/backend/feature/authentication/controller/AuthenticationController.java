package net.furizon.backend.feature.authentication.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.dto.RegisterUserResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    @PostMapping("/register")
    public RegisterUserResponse registerUser(
        @RequestBody final RegisterUserRequest registerUserRequest
    ) {
        return new RegisterUserResponse(true);
    }
}
