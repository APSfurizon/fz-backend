package net.furizon.backend.pretix.property;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pretix")
public record PretixProperties(
    int port,
    @NotNull
    String host,
    @NotNull
    String secret
) {
}
