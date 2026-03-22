package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.action.uploads.selectUpload.SelectUploadAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectUploadUseCase implements UseCase<SelectUploadUseCase.Input, Boolean> {
    @NotNull
    private final SelectUploadAction selectUploadAction;


    @Override
    public @NotNull Boolean executor(@NotNull SelectUploadUseCase.Input input) {
        log.info("User {} is selecting upload {} for its event", input.furizonUser.getUserId(), input.uploadId);
        selectUploadAction.invoke(input.uploadId);
        return true;
    }

    public record Input(
            long uploadId,
            @NotNull FurizonUser furizonUser
    ) {}
}
