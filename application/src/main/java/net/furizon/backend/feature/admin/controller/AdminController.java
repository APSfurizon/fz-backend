package net.furizon.backend.feature.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.CapabilitiesResponse;
import net.furizon.backend.feature.admin.usecase.GetCapabilitiesUseCase;
import net.furizon.backend.infrastructure.media.DeleteMediaCronjob;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @org.jetbrains.annotations.NotNull
    private final DeleteMediaFromDiskAction deleteMediaAction;
    @org.jetbrains.annotations.NotNull
    private final DeleteMediaCronjob deleteMediaCronjob;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @Operation(summary = "Gets what an user can do in the admin panel", description =
        "This method should be used to display or not buttons in the admin panel of the "
        + "reserved area")
    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @GetMapping("/capabilities")
    public CapabilitiesResponse getCapabilities(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(GetCapabilitiesUseCase.class, user);
    }

    @PermissionRequired(permissions = {Permission.CAN_MANAGE_RAW_UPLOADS})
    @DeleteMapping("/media/")
    public boolean deleteMedias(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @RequestParam("id") Set<Long> ids
    ) {
        try {
            return deleteMediaAction.invoke(ids, true);
        } catch (IOException e) {
            log.error("Error while deleting medias", e);
            return false;
        }
    }

    @PermissionRequired(permissions = {Permission.CAN_MANAGE_RAW_UPLOADS})
    @PostMapping("/media/run-delete-media-cronjob")
    public void runDeleteMediaCronjob(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        deleteMediaCronjob.deleteDanglingMedia();
    }
}
