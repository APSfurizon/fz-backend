package net.furizon.backend.web.controllers.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.service.users.UserService;
import net.furizon.backend.web.entities.users.UserLoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
@Slf4j
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login (HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody UserLoginRequest body) {
        userService.login(request, response, body);

    }

}
