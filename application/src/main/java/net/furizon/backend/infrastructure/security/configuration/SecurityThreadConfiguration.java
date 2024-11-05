package net.furizon.backend.infrastructure.security.configuration;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.concurrent.MdcTaskDecorator;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static net.furizon.backend.infrastructure.security.Const.SESSION_THREAD_POOL_TASK_EXECUTOR;

@Configuration
@RequiredArgsConstructor
public class SecurityThreadConfiguration {
    private final SecurityConfig securityConfig;

    @Bean(SESSION_THREAD_POOL_TASK_EXECUTOR)
    public Executor sessionThreadPoolTaskExecutor() {
        final var corePoolUpdateSize = securityConfig.getSession().getCorePoolUpdateSize();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolUpdateSize);
        executor.setMaxPoolSize(corePoolUpdateSize);
        executor.setThreadNamePrefix("session-thread-");
        executor.setTaskDecorator(new MdcTaskDecorator());

        return executor;
    }
}
