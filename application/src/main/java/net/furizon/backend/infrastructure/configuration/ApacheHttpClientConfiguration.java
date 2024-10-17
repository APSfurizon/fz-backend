package net.furizon.backend.infrastructure.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient5.LogbookHttpExecHandler;

@Configuration
public class ApacheHttpClientConfiguration {
    @Bean
    @Scope(scopeName = "prototype")
    public CloseableHttpClient closeableHttpClient(@NotNull final Logbook logbook) {
        return HttpClients.custom()
            .addExecInterceptorFirst("Logbook", new LogbookHttpExecHandler(logbook))
            .build();
    }
}
