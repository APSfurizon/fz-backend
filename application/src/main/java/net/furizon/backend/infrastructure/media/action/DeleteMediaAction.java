package net.furizon.backend.infrastructure.media.action;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DeleteMediaAction {
    /**
     * @param ids - IDs of medias to be deleted
     * @return true whenever the deletion is completed
     */
    boolean deleteFromDb(@NotNull List<Long> ids);
}
