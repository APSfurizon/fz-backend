package net.furizon.backend.feature.badge.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.badge.usecase.DeleteBadgeUseCase;
import net.furizon.backend.feature.badge.usecase.UploadBadgeUsecase;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/badge")
@RequiredArgsConstructor
public class BadgeController {
    private final UseCaseExecutor useCaseExecutor;

    //Serving the files is handled by nginx itself

    @Operation(summary = "Uploads the user's badge", description =
        "This method excepts the badge to be correctly cropped and resized. "
        + "If the ratio is not 1:1, the image will be cropped top left. If it has "
        + "an invalid size or dimensions, we will return with an error. We return "
        + "the media id and the relative path where the file is served")
    @PostMapping(value = "/user/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaResponse userUpload(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @RequestParam("image") MultipartFile image
    ) {
        return useCaseExecutor.execute(
            UploadBadgeUsecase.class,
            new UploadBadgeUsecase.Input(
                user,
                image,
                BadgeType.BADGE_USER,
                null
            )
        );
    }
    @Operation(summary = "Uploads the fursuit's badge", description =
            "Using "
            + "This method excepts the badge to be correctly cropped and resized. "
            + "If the ratio is not 1:1, the image will be cropped top left. If it has "
            + "an invalid size or dimensions, we will return with an error. We return "
            + "the media id and the relative path where the file is served")
    @PostMapping(value = "/fursuit/upload/{fursuitId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaResponse fursuitUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId,
            @RequestParam("image") MultipartFile image
    ) {
        return useCaseExecutor.execute(
            UploadBadgeUsecase.class,
            new UploadBadgeUsecase.Input(
                user,
                image,
                BadgeType.BADGE_FURSUIT,
                fursuitId
            )
        );
    }



    @DeleteMapping(value = "/user/")
    public boolean deleteUserUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return useCaseExecutor.execute(
                DeleteBadgeUseCase.class,
                new DeleteBadgeUseCase.Input(
                        user,
                        BadgeType.BADGE_USER,
                        null
                )
        );
    }
    @DeleteMapping(value = "/fursuit/{fursuitId}")
    public boolean deleteFursuitUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @PathVariable("fursuitId") final long fursuitId
    ) {
        return useCaseExecutor.execute(
                DeleteBadgeUseCase.class,
                new DeleteBadgeUseCase.Input(
                        user,
                        BadgeType.BADGE_FURSUIT,
                        fursuitId
                )
        );
    }
}
