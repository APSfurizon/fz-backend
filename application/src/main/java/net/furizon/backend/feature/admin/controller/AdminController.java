package net.furizon.backend.feature.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.admin.dto.CapabilitiesResponse;
import net.furizon.backend.feature.admin.usecase.GetCapabilitiesUseCase;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

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
}
