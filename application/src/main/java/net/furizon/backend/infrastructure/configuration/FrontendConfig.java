package net.furizon.backend.infrastructure.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    @Getter(AccessLevel.NONE)
    @NotNull private String loginRedirectUrl;

    @NotNull
    public String getLoginRedirectUrl(@NotNull final String redirectToUrl) {
        return loginRedirectUrl + URLEncoder.encode(redirectToUrl, StandardCharsets.UTF_8);
    }
}
