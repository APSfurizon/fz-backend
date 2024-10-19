package net.furizon.backend.feature.user.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restapi/v0/users")
@RequiredArgsConstructor
public class SimpleUserController {
    private final UseCaseExecutor executor;

}
