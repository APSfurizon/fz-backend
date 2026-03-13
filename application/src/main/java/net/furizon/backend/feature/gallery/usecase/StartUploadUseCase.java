package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.request.StartUploadRequest;
import net.furizon.backend.feature.gallery.dto.response.StartUploadResponse;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartUploadUseCase implements UseCase<StartUploadResponse, StartUploadUseCase.Input> {


    @Override
    public @NotNull Input executor(@NotNull StartUploadResponse input) {
        return null;
    }

    public record Input(
            @NotNull StartUploadRequest req,
            @NotNull FurizonUser user
    ) {}
}
