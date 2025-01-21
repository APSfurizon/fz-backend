package net.furizon.backend.feature.nosecount.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counts")
@RequiredArgsConstructor
public class CountsController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;
}
