package net.furizon.backend.feature.user.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SimpleUserController {
    @GetMapping
    public User getMe() {
        return User.builder().id(1L).build();
    }
}
