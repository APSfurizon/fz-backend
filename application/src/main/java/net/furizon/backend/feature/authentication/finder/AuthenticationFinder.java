package net.furizon.backend.feature.authentication.finder;

import net.furizon.backend.feature.authentication.Authentication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AuthenticationFinder {
    @Nullable
    Authentication findByEmail(@NotNull String email);
}
