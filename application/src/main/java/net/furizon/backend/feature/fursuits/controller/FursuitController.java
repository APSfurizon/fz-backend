package net.furizon.backend.feature.fursuits.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.usecase.DeleteBadgeUseCase;
import net.furizon.backend.feature.badge.usecase.UploadBadgeUsecase;
import net.furizon.backend.feature.fursuits.dto.AddFursuitBadgesRequest;
import net.furizon.backend.feature.fursuits.dto.BringFursuitToEventRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.dto.FursuitDataRequest;
import net.furizon.backend.feature.fursuits.dto.FursuitListResponse;
import net.furizon.backend.feature.fursuits.dto.MultipleBringFursuitToEventRequest;
import net.furizon.backend.feature.fursuits.usecase.AddFursuitBadgesUseCase;
import net.furizon.backend.feature.fursuits.usecase.BringFursuitToEventUseCase;
import net.furizon.backend.feature.fursuits.usecase.CreateFursuitUseCase;
import net.furizon.backend.feature.fursuits.usecase.DeleteFursuitUseCase;
import net.furizon.backend.feature.fursuits.usecase.GetAllFursuitsUseCase;
import net.furizon.backend.feature.fursuits.usecase.GetSingleFursuitUseCase;
import net.furizon.backend.feature.fursuits.usecase.MultipleBringFursuitToEventUseCase;
import net.furizon.backend.feature.fursuits.usecase.UpdateFursuitDataUseCase;
import net.furizon.backend.infrastructure.GeneralConsts;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequiredMode;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/fursuits")
@RequiredArgsConstructor
public class FursuitController {
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;
    private final TranslationService translationService;

    @Operation(summary = "Fetch data for the specified fursuit", description =
        "If we're unable to retrieve the specified fursuit, we return a "
        + "`FURSUIT_NOT_FOUND` error")
    @GetMapping("/{fursuitId}")
    public @NotNull FursuitData getFursuit(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") @NotNull final Long fursuitId
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
            @PathVariable("fursuitId") @NotNull final Long fursuitId
    ) {
        return executor.execute(
                DeleteFursuitUseCase.class,
                new DeleteFursuitUseCase.Input(
                        user,
                        fursuitId,
                        pretixInformation.getCurrentEvent()
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
        + "Editing this field is not permitted after the badge editing deadline. To understand if "
        + "an user can update it, use the `allowEditBringFursuitToEvent` field of the GET /badge/ endpoint")
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

    @Operation(summary = "Sets on multiple fursuits if the user is bringing them to the current event", description =
        "Specify a map of fursuitId -> bringToCurrent event for updating the value on multiple fursuits atomically. "
        + "Check the `/{fursuitId}/bringToEvent` for more information. If the operation is performed from an "
        + "administrator, he must specify the userId of the owner of the fursuits. Only the fursuits of one "
        + "owner per time can be updated together.")
    @PostMapping("/bringToEvent")
    public boolean multipleBringFursuitToEvent(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @RequestBody @Valid @NotNull final MultipleBringFursuitToEventRequest req
    ) {
        return executor.execute(
                MultipleBringFursuitToEventUseCase.class,
                new MultipleBringFursuitToEventUseCase.Input(
                        req,
                        user,
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Updates the specified fursuit without image", description =
        "This method doesn't support the immediate fursuit image reupload or deletion, which can "
        + "be done later using the /badge endpoint. With this method data should be "
        + "passed as usual, as a JSON object in the post/request body. "
        + "Except a `FURSUIT_BADGES_ENDED` error if the user had ended "
        + "the available fursuit badges for the current event"
        + "To check whenever or not an user can bring another fursuit to the event, check "
        + "the `canBringFursuitsToEvent` field of the /badge endpoint"
        + "To bring a fursuit to an event the user needs to have an order in the "
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors. "
        + "Editing the field `bringToCurrentEvent` is not permitted after the badge editing deadline. To understand if "
        + "an user can update it, use the `allowEditBringFursuitToEvent` field of the GET /badge/ endpoint")
    @PostMapping("/{fursuitId}")
    public @NotNull FursuitData updateFursuit(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") @NotNull final Long fursuitId,
            @RequestBody @NotNull @Valid final FursuitDataRequest req
    ) {
        FursuitData data = executor.execute(
                UpdateFursuitDataUseCase.class,
                new UpdateFursuitDataUseCase.Input(
                        fursuitId,
                        req.getName(),
                        req.getSpecies(),
                        req.getShowInFursuitCount(),
                        req.getShowOwner(),
                        pretixInformation.getCurrentEvent(),
                        user
                )
        );
        if (data.isBringingToEvent() != req.getBringToCurrentEvent()) {
            executor.execute(
                BringFursuitToEventUseCase.class,
                new BringFursuitToEventUseCase.Input(
                    new BringFursuitToEventRequest(req.getBringToCurrentEvent()),
                    fursuitId,
                    user,
                    pretixInformation
                )
            );
        }
        return data;
    }

    @Operation(summary = "Updates the specified fursuit allowing also propic update", description =
        "This method **supports** the immediate fursuit image reupload by passing it in the "
        + "`image` form parameters. To delete the propic, set `deleteImage` to true; this flag "
        + "will have priority over reuploads! "
        + "With this method data should be passed as a multipart/form-data"
        + "Except a `FURSUIT_BADGES_ENDED` error if the user had ended "
        + "the available fursuit badges for the current event. "
        + "To check whenever or not an user can bring another fursuit to the event, check "
        + "the `canBringFursuitsToEvent` field of the /badge endpoint"
        + "To bring a fursuit to an event the user needs to have an order in the "
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors. "
        + "Editing the field `bringToCurrentEvent` is not permitted after the badge editing deadline. To understand if "
        + "an user can update it, use the `allowEditBringFursuitToEvent` field of the GET /badge/ endpoint")
    @PostMapping("/{fursuitId}/update-with-image")
    public @NotNull FursuitData updateFursuitWithImage(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("fursuitId") @NotNull final Long fursuitId,
            @Pattern(regexp = GeneralConsts.NAME_REGEX)
            @Valid @NotNull @RequestParam("name") final String name,
            @Pattern(regexp = GeneralConsts.NAME_REGEX)
            @Valid @Nullable @RequestParam("species") final String species,
            @RequestParam("bring-to-current-event") @NotNull final Boolean bringToCurrentEvent,
            @RequestParam("show-in-fursuit-count") @NotNull final Boolean showInFursuitCount,
            @RequestParam("show-owner") @NotNull final Boolean showOwner,
            @RequestParam("delete-image") @NotNull final Boolean deleteImage,
            @RequestParam(value = "image", required = false) @jakarta.annotation.Nullable MultipartFile image
    ) {
        FursuitData data = executor.execute(
                UpdateFursuitDataUseCase.class,
                new UpdateFursuitDataUseCase.Input(
                        fursuitId,
                        name,
                        species,
                        showInFursuitCount,
                        showOwner,
                        pretixInformation.getCurrentEvent(),
                        user
                )
        );
        if (data.isBringingToEvent() != bringToCurrentEvent) {
            executor.execute(
                    BringFursuitToEventUseCase.class,
                    new BringFursuitToEventUseCase.Input(
                            new BringFursuitToEventRequest(bringToCurrentEvent),
                            fursuitId,
                            user,
                            pretixInformation
                    )
            );
        }
        if (deleteImage) {
            executor.execute(
                    DeleteBadgeUseCase.class,
                    new DeleteBadgeUseCase.Input(
                            user.getUserId(),
                            BadgeType.BADGE_FURSUIT,
                            fursuitId,
                            pretixInformation.getCurrentEvent(),
                            true
                    )
            );
            data.getFursuit().setPropic(null);

        } else if (image != null) {
            MediaResponse media = executor.execute(
                    UploadBadgeUsecase.class,
                    new UploadBadgeUsecase.Input(
                            user,
                            image,
                            BadgeType.BADGE_FURSUIT,
                            fursuitId,
                            pretixInformation.getCurrentEvent(),
                            true
                    )
            );
            data.getFursuit().setPropic(media);
        }
        return data;
    }

    @Operation(summary = "Gets fursuit information for a user", description =
        "The various information returned by this method is the full list of fursuits "
        + "owned by an user, if the user can bring fursuit to an event and if he can "
        + "actually change that, and how many fursuits he can currently bring with him. "
        + "An admin can use the optional query parameter `userId` to specify the user he "
        + "wants to get the fursuits of")
    @GetMapping("/")
    public @NotNull FursuitListResponse getAllFursuits(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @RequestParam("user-id") @Nullable final Long userId
    ) {
        return executor.execute(
                GetAllFursuitsUseCase.class,
                new GetAllFursuitsUseCase.Input(
                        user,
                        userId,
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
        + "'paid' status, so expect also `ORDER_NOT_PAID` and `ORDER_NOT_FOUND` errors."
        + "Creating a fursuit with the field `bringToCurrentEvent` is not permitted after the badge editing deadline."
        + "To understand if "
        + "an user can update it, use the `allowEditBringFursuitToEvent` field of the GET /badge/ endpoint")
    @PostMapping("/")
    public @NotNull FursuitData addFursuit(
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
        @RequestBody @NotNull @Valid final FursuitDataRequest req
    ) {
        return executor.execute(
                CreateFursuitUseCase.class,
                new CreateFursuitUseCase.Input(
                        req.getName(),
                        req.getSpecies(),
                        req.getBringToCurrentEvent(),
                        req.getShowInFursuitCount(),
                        req.getUserId(),
                        req.getShowOwner(),
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
        + "Creating a fursuit with the field `bringToCurrentEvent` is not permitted after the badge editing deadline."
        + "To understand if "
        + "an user can update it, use the `allowEditBringFursuitToEvent` field of the GET /badge/ endpoint")
    @PostMapping("/add-with-image")
    public @NotNull FursuitData addFursuit(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @Pattern(regexp = GeneralConsts.NAME_REGEX)
        @Valid @NotNull @RequestParam("name") final String name,
        @Pattern(regexp = GeneralConsts.NAME_REGEX)
        @Valid @Nullable @RequestParam("species") final String species,
        @RequestParam("bring-to-current-event") @NotNull final Boolean bringToCurrentEvent,
        @RequestParam("show-in-fursuit-count") @NotNull final Boolean showInFursuitCount,
        @Nullable @RequestParam(value = "user-id", required = false) final Long userId,
        @RequestParam("show-owner") @NotNull final Boolean showOwner,
        @Nullable @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        FursuitData data = executor.execute(
                CreateFursuitUseCase.class,
                new CreateFursuitUseCase.Input(
                        name,
                        species,
                        bringToCurrentEvent,
                        showInFursuitCount,
                        userId,
                        showOwner,
                        user,
                        pretixInformation
                )
        );
        if (image != null) {
            MediaResponse media = executor.execute(
                    UploadBadgeUsecase.class,
                    new UploadBadgeUsecase.Input(
                            user,
                            image,
                            BadgeType.BADGE_FURSUIT,
                            data.getFursuit().getId(),
                            pretixInformation.getCurrentEvent(),
                            true
                    )
            );
            data.getFursuit().setPropic(media);
        }
        return data;
    }

    @Operation(summary = "Adds extra fursuit badges to the order of the specified user", description =
        "This method, for admins with permission `CAN_PERFORM_CHECKINS` or `PRETIX_ADMIN`, is meant to "
        + "easily add from the backend extra fursuit badges to the order of the specified user. "
        + "Quantity is selectable. There's also the possibility to mark the newly added badges as paid "
        + "directly in the pretix order, in cases where the payment happens via other channels. If, "
        + "by adding the new badges to the order, `max-backend-fursuits-no` is reached, we return "
        + "the error `TOO_MANY_EXTRA_FURSUIT_BADGES`. On success, we return the updated FursuitListResponse object")
    @PermissionRequired(permissions = {
        Permission.CAN_PERFORM_CHECKINS,
        Permission.PRETIX_ADMIN
    }, mode = PermissionRequiredMode.ANY)
    @PostMapping("/add-fursuit-badges")
    public @NotNull FursuitListResponse  addFursuitBadges(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @RequestBody @NotNull @Valid final AddFursuitBadgesRequest addFursuitBadgesRequest
    ) {
        boolean b = executor.execute(
                AddFursuitBadgesUseCase.class,
                new AddFursuitBadgesUseCase.Input(
                        user,
                        pretixInformation,
                        addFursuitBadgesRequest
                )
        );
        if (!b) {
            log.error("AddFursuitBadgesUseCase returned false");
            throw new ApiException(
                    translationService.error("common.server_error"),
                    GeneralResponseCodes.GENERIC_ERROR
            );
        }
        return getAllFursuits(user, addFursuitBadgesRequest.getTargetUserId());
    }
}
