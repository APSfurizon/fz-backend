package net.furizon.backend.feature.badge.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.usecase.DeleteUserBadgeUsecase;
import net.furizon.backend.feature.badge.usecase.UploadUserBadgeUsecase;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/badge")
@RequiredArgsConstructor
public class BadgeController {
    private final UseCaseExecutor useCaseExecutor;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BadgeUploadResponse upload(
        @AuthenticationPrincipal @NotNull final FurizonUser user,
        @RequestParam("image") MultipartFile image
    ) {
        return useCaseExecutor.execute(
            UploadUserBadgeUsecase.class,
            new UploadUserBadgeUsecase.Input(
                user,
                image
            )
        );
    }

    @DeleteMapping(value = "/")
    public boolean deleteUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @RequestParam("id")Set<Long> ids
    ) {
        return useCaseExecutor.execute(
            DeleteUserBadgeUsecase.class,
            ids
        );
    }
}
