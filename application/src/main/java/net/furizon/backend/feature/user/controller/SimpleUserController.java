package net.furizon.backend.feature.user.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.finder.UserFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO -> Remove, just for demo
@RestController
@RequestMapping("/restapi/v0/users")
@RequiredArgsConstructor
public class SimpleUserController {
    private final UserFinder userFinder;

    @GetMapping
    public List<User> getAll() {
        return userFinder.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> findById(
        @PathVariable long userId
    ) {
        return ResponseEntity.ofNullable(userFinder.findUserById(userId));
    }
}
