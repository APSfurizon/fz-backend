package net.furizon.backend.infrastructure.s3.actions.objectExists;

import org.jetbrains.annotations.NotNull;

public interface S3KeyExists {
    boolean invoke(@NotNull String key);
}
