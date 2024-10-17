package net.furizon.backend.infrastructure.usecase;

import org.jetbrains.annotations.NotNull;

public interface UseCaseExecutor {
    @NotNull
    <I, R, U extends UseCase<I, R>> R execute(
        @NotNull final Class<U> useCaseClass,
        @NotNull final I input
    );
}
