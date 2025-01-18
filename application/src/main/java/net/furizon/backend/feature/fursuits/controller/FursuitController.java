package net.furizon.backend.feature.fursuits.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.usecase.UploadBadgeUsecase;
import net.furizon.backend.feature.fursuits.dto.BringFursuitToEventRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitDataRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.usecase.BringFursuitToEventUseCase;
import net.furizon.backend.feature.fursuits.usecase.CreateFursuitUseCase;
import net.furizon.backend.feature.fursuits.usecase.DeleteFursuitUseCase;
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

    @Operation(summary = "Deletes the specified fursuit", description =
            "If we're unable to retrieve the specified fursuit, we return a "
            + "`FURSUIT_NOT_FOUND` error")
    @DeleteMapping("/{fursuitId}")
    public boolean deleteFursuit(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId
    ) {
        return executor.execute(
                DeleteFursuitUseCase.class,
                new DeleteFursuitUseCase.Input(
                        user,
                        fursuitId
                )
        );
    }

    @Operation(summary = "Sets or not if the user is bringing the specified fursuit to the current event", description =
        "If we're unable to retrieve the specified fursuit, we return a "
        + "`FURSUIT_NOT_FOUND` error. There's a max limit on how many fursuit "
        + "an user can bring to an event, so expect a `FURSUIT_BADGES_ENDED` error. "
        + "To check whenever or not an user can bring another fursuit to the event, check "
        + "the `canBringFursuitsToEvent` field of the /badge endpoint"
        + "To bring a fursuit to an event the user needs to have an order in the "
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors. "
        + "Using the field `userId` an admin must specify the owner of the fursuit")
    @PostMapping("/{fursuitId}/bringToEvent")
    public boolean bringFursuitToEvent(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId,
            @RequestBody @Valid @NotNull final BringFursuitToEventRequest req
    ) {
        return executor.execute(
                BringFursuitToEventUseCase.class,
                new BringFursuitToEventUseCase.Input(
                        req,
                        fursuitId,
                        user,
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Creates a new fursuit without image", description =
        "This method doesn't support the immediate fursuit image upload, which can "
        + "be done later using the /badge endpoint. With this method data should be "
        + "passed as usual, as a JSON object in the post/request body. "
        + "Except a `FURSUIT_BADGES_ENDED` error if the user had ended "
        + "the available fursuit badges for the current event"
        + "To check whenever or not an user can bring another fursuit to the event, check "
        + "the `canBringFursuitsToEvent` field of the /badge endpoint"
        + "To bring a fursuit to an event the user needs to have an order in the "
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors")
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

    @Operation(summary = "Creates a new fursuit with an image", description =
        "This method **supports** the immediate fursuit image upload "
        + "With this method data should be passed as a multipart/form-data"
        + "Except a `FURSUIT_BADGES_ENDED` error if the user had ended "
        + "the available fursuit badges for the current event. "
        + "To check whenever or not an user can bring another fursuit to the event, check "
        + "the `canBringFursuitsToEvent` field of the /badge endpoint"
        + "To bring a fursuit to an event the user needs to have an order in the "
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors")
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
