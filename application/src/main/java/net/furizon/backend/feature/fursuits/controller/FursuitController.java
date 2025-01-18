package net.furizon.backend.feature.fursuits.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.usecase.GetSingleFursuitUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fursuits")
@RequiredArgsConstructor
public class FursuitController {
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @Operation(summary = "Fetch data for the specified fursuit", description =
        "If we're unable to retrieve the specified fursuit, we return a "
        + "`FURSUIT_NOT_FOUND` error")
    @GetMapping("/{fursuitId}")
    public @NotNull FursuitDisplayData getFursuit(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId
    ) {
        return executor.execute(
                GetSingleFursuitUseCase.class,
                new GetSingleFursuitUseCase.Input(
                        fursuitId,
                        user,
                    pretixInformation.getCurrentEvent()
                )
        );
    }
}
