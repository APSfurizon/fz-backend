package net.furizon.backend.feature.badge.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.dto.FullInfoBadgeResponse;
import net.furizon.backend.feature.badge.dto.UpdateUserBadgeRequest;
import net.furizon.backend.feature.badge.usecase.DeleteBadgeUseCase;
import net.furizon.backend.feature.badge.usecase.GetFullInfoBadgeUseCase;
import net.furizon.backend.feature.badge.usecase.UpdateUserBadgeInfoUseCase;
import net.furizon.backend.feature.badge.usecase.UploadBadgeUsecase;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping("/api/v1/badge")
@RequiredArgsConstructor
public class BadgeController {
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor useCaseExecutor;

    //Serving the files is handled by nginx itself

    @Operation(summary = "Uploads the user's badge", description =
        "This method excepts the badge to be correctly cropped and resized. "
        + "If the ratio is not 1:1, the image will be cropped top left. If it has "
        + "an invalid size or dimensions, we will return with an error. We return "
        + "the media id and the relative path where the file is served")
    @PostMapping(value = "/user/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @NotNull MediaResponse userUpload(
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
        @RequestParam("image") @NotNull MultipartFile image
    ) {
        return useCaseExecutor.execute(
            UploadBadgeUsecase.class,
            new UploadBadgeUsecase.Input(
                user,
                image,
                BadgeType.BADGE_USER,
                null,
                pretixInformation.getCurrentEvent()
            )
        );
    }
    @Operation(summary = "Uploads the specified user's badge", description =
        "This method is intended for admin use only"
        + "This method excepts the badge to be correctly cropped and resized. "
        + "If the ratio is not 1:1, the image will be cropped top left. If it has "
        + "an invalid size or dimensions, we will return with an error. We return "
        + "the media id and the relative path where the file is served")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @PostMapping(value = "/user/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @NotNull MediaResponse userUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable @Valid @NotNull final Long userId,
            @RequestParam("image") @NotNull MultipartFile image
    ) {
        FurizonUser destUser = FurizonUser.builder()
                .userId(userId)
                .sessionId(user.getSessionId())
                .authentication(user.getAuthentication())
            .build();
        return useCaseExecutor.execute(
                UploadBadgeUsecase.class,
                new UploadBadgeUsecase.Input(
                        destUser,
                        image,
                        BadgeType.BADGE_USER,
                        null
                )
        );
    }
    @Operation(summary = "Uploads the fursuit's badge", description =
            "This method excepts the badge to be correctly cropped and resized. "
            + "If the ratio is not 1:1, the image will be cropped top left. If it has "
            + "an invalid size or dimensions, we will return with an error. We return "
            + "the media id and the relative path where the file is served")
    @PostMapping(value = "/fursuit/upload/{fursuitId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @NotNull MediaResponse fursuitUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId,
            @RequestParam("image") @NotNull MultipartFile image
    ) {
        return useCaseExecutor.execute(
            UploadBadgeUsecase.class,
            new UploadBadgeUsecase.Input(
                user,
                image,
                BadgeType.BADGE_FURSUIT,
                fursuitId,
                pretixInformation.getCurrentEvent()
            )
        );
    }

    @Operation(summary = "Deletes the user's badge")
    @DeleteMapping(value = "/user/")
    public boolean deleteUserUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return useCaseExecutor.execute(
                DeleteBadgeUseCase.class,
                new DeleteBadgeUseCase.Input(
                        user.getUserId(),
                        BadgeType.BADGE_USER,
                        null
                )
        );
    }
    @Operation(summary = "Deletes the specified user's badge", description =
        "This is intended for admin use only")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @DeleteMapping(value = "/user/{userId}")
    public boolean deleteUserUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable @Valid @NotNull final Long userId
    ) {
        return useCaseExecutor.execute(
                DeleteBadgeUseCase.class,
                new DeleteBadgeUseCase.Input(
                        userId,
                        BadgeType.BADGE_USER,
                        null,
                        pretixInformation.getCurrentEvent()
                )
        );
    }
    @DeleteMapping(value = "/fursuit/{fursuitId}")
    public boolean deleteFursuitUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @PathVariable("fursuitId") @NotNull final Long fursuitId
    ) {
        //Permission checks done inside
        return useCaseExecutor.execute(
                DeleteBadgeUseCase.class,
                new DeleteBadgeUseCase.Input(
                        user.getUserId(),
                        BadgeType.BADGE_FURSUIT,
                        fursuitId,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Returns the full info to be displayed on the badge page", description =
        "This method returns information about: when the badges are going to be physically printed, so any "
        + "further changes won't be on the actual physical badge; "
        + "current user data, so its propic, fursona name, etc; "
        + "how many fursuits the user can 'legally' bring to the event EG "
        + "how many fursuits he has bought + default fursuit no; full list of fursuits the user has"
        + " + if the user has marked that fursuit to be brought to the current event. "
        + "If `allowedModifications` is set to false, the frontend should prevent the user from performing ANY "
        + "actions on both badge information (EG fursona name, locale), badge propic and all fursuits interactions")
    @GetMapping("/")
    public @NotNull FullInfoBadgeResponse getBadge(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return useCaseExecutor.execute(
                GetFullInfoBadgeUseCase.class,
                new GetFullInfoBadgeUseCase.Input(
                        user.getUserId(),
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Updates the badge info of the user", description =
        "By specifying the field `userId` in the request, an administrator can "
        + "update the information of another user. For normal people that field can "
        + "be simply omitted")
    @PostMapping("/update-user-badge-info")
    public boolean updateUserBadgeInfo(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @RequestBody @Valid @NotNull final UpdateUserBadgeRequest req
    ) {
        return useCaseExecutor.execute(
                UpdateUserBadgeInfoUseCase.class,
                new UpdateUserBadgeInfoUseCase.Input(
                        user,
                        req,
                        pretixInformation.getCurrentEvent()
                )
        );
    }
}
