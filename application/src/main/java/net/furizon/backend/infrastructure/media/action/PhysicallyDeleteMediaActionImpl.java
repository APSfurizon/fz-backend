package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhysicallyDeleteMediaActionImpl implements PhysicallyDeleteMediaAction {
    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;

    @Override
    public boolean invoke(@NotNull MediaData media, boolean deleteFromDb) {
        try {
            boolean res = switch (media.getStoreMethod()) {
                //It internally calls deleteIfExists
                case StoreMethod.DISK -> deleteMediaFromDiskAction.invoke(media, deleteFromDb);
                default -> throw new IllegalStateException("Unexpected value: " + media.getStoreMethod());
            };
            if (!res) {
                log.error("Failed to delete media: {}", media);
            }
            return res;
        } catch (IOException e) {
            log.error("Error deleting dangling media {}. store={} path={}",
                    media.getId(), media.getStoreMethod(), media.getPath(), e);
            return false;
        }
    }
}
