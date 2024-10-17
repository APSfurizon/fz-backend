package net.furizon.backend.infrastructure.usecase;

import org.jetbrains.annotations.NotNull;

public interface UseCase<I, R> {
    @NotNull
    R executor(@NotNull I input);
}
