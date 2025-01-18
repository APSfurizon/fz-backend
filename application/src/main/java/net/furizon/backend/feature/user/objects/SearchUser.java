package net.furizon.backend.feature.user.objects;

import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SearchUser(long id, @NotNull String description, @Nullable MediaResponse propic){}