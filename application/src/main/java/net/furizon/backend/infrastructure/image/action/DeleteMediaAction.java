package net.furizon.backend.infrastructure.image.action;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DeleteMediaAction {
    /**
     * @param ids - IDs of medias to be deleted
     * @return true whenever the deletion is completed
     */
    boolean invoke(@NotNull List<Long> ids);
}
