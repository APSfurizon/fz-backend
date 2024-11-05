package net.furizon.backend.feature.user.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.FurizonUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SimpleUserController {
    @GetMapping("/me")
    public FurizonUser getMe(
        @AuthenticationPrincipal @NotNull FurizonUser user
    ) {
        return user;
    }
}
