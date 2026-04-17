package net.furizon.backend.feature.gallery.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.usecase.processor.JobCompletedWebhookUseCase;
import net.furizon.backend.infrastructure.security.annotation.InternalAuthorize;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/gallery")
@RequiredArgsConstructor
public class GalleryProcessorController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @Operation(summary = "Internal webhook for gallery processor, to notify completed jobs")
    @PostMapping("/job-completed")
    @InternalAuthorize
    public boolean jobDone(@Valid @NotNull @RequestBody GalleryProcessorJob job) {
        return executor.execute(
                JobCompletedWebhookUseCase.class,
                job
        );
    }
}
