package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.uploads.deselectUpload.DeselectUploadAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeselectUploadUseCase implements UseCase<DeselectUploadUseCase.Input, Boolean> {
    @NotNull
    private final DeselectUploadAction deselectUploadAction;


    @Override
    public @NotNull Boolean executor(@NotNull DeselectUploadUseCase.Input input) {
        log.info("User {} is deselecting upload {}", input.furizonUser.getUserId(), input.uploadId);
        deselectUploadAction.invoke(input.uploadId);
        return true;
    }

    public record Input(
            long uploadId,
            @NotNull FurizonUser furizonUser
    ) {}
}
