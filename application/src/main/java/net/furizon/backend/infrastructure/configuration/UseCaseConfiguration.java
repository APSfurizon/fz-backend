package net.furizon.backend.infrastructure.configuration;

import net.furizon.backend.infrastructure.usecase.SimpleUseCaseExecutor;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class UseCaseConfiguration {
    @Bean
    public UseCaseExecutor useCaseExecutor(@Nullable final List<UseCase<?, ?>> useCases) {
        return new SimpleUseCaseExecutor(
            useCases != null
                ? useCases
                : Collections.emptyList()
        );
    }
}
