package net.furizon.backend.feature.user.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SearchUser(long id, @NotNull String description, @Nullable String imageUrl){}