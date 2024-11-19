package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    @NotNull private String loginRedirectUrl;
}
