package net.furizon.backend.feature.fursuits.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.usecase.UploadBadgeUsecase;
import net.furizon.backend.feature.fursuits.dto.FursuitDataRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.usecase.CreateFursuitUseCase;
import net.furizon.backend.feature.fursuits.usecase.GetSingleFursuitUseCase;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
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

    @PostMapping("/add")
    public @NotNull FursuitDisplayData addFursuit(
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
        @RequestBody @NotNull @Valid final FursuitDataRequest req
    ) {
        return executor.execute(
                CreateFursuitUseCase.class,
                new CreateFursuitUseCase.Input(
                        req.getName(),
                        req.getSpecies(),
                        req.isBringToCurrentEvent(),
                        user,
                        pretixInformation
                )
        );
    }

    @PostMapping("/add-with-image")
    public @NotNull FursuitDisplayData addFursuit(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$")
        @Valid @NotNull @RequestParam("name") final String name,
        @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$")
        @Valid @NotNull @RequestParam("species") final String species,
        final boolean bringToCurrentEvent,
        @RequestParam("image") MultipartFile image
    ) {
        FursuitDisplayData data = executor.execute(
                CreateFursuitUseCase.class,
                new CreateFursuitUseCase.Input(
                        name,
                        species,
                        bringToCurrentEvent,
                        user,
                        pretixInformation
                )
        );
        if (data != null && image != null) {
            MediaResponse media = executor.execute(
                    UploadBadgeUsecase.class,
                    new UploadBadgeUsecase.Input(
                            user,
                            image,
                            BadgeType.BADGE_FURSUIT,
                            data.getId()
                    )
            );
            data.setPropic(media);
        }
        return data;
    }
}
