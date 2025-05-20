package net.furizon.backend.infrastructure.admin;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("admin")
public class AdminConfig {
    @NotNull private final String customJteTemplatesLocation;
    @NotNull private final String jteRuntimeJarLocation;
}
