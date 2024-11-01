package net.furizon.backend.infrastructure.usecase;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Slf4j
public class SimpleUseCaseExecutor implements UseCaseExecutor {
    @NotNull
    private final Map<String, UseCase<?, ?>> useCaseMap;

    public SimpleUseCaseExecutor(@NotNull final List<UseCase<?, ?>> useCases) {
        useCaseMap = useCases
            .stream()
            .collect(
                Collectors.toMap(
                    (useCase) -> useCase.getClass().getCanonicalName().split("\\$\\$")[0],
                    Function.identity()
                )
            );
    }

    @NotNull
    @Override
    public <I, R, U extends UseCase<I, R>> R execute(
        @NotNull Class<U> useCaseClass,
        @NotNull I input
    ) {
        log.debug("Executing use case: {}", useCaseClass.getSimpleName());
        final var startTime = System.currentTimeMillis();
        final UseCase<?, ?> useCase = useCaseMap.get(useCaseClass.getCanonicalName());
        if (useCase == null) {
            throw new IllegalArgumentException("Use case not found: " + useCaseClass.getSimpleName());
        }

        final var result = ((UseCase<I, R>) useCase).executor(input);

        log.debug(
            "Use case '{}' finished with {} ms",
            useCaseClass.getSimpleName(),
            System.currentTimeMillis() - startTime
        );

        return result;
    }
}
