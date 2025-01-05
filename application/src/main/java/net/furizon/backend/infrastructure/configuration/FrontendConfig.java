package net.furizon.backend.infrastructure.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Data
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    @Getter(AccessLevel.NONE)
    @NotNull private String loginUrl;

    @Getter(AccessLevel.NONE)
    @NotNull private String loginRedirectUrl;

    @Getter(AccessLevel.NONE)
    @NotNull private String orderHomepageUrl;

    @Getter(AccessLevel.NONE)
    @NotNull private String passwordResetUrl;


    @NotNull
    public String getLoginUrl() {
        return getLoginUrl(null);
    }
    @NotNull
    public String getLoginUrl(@Nullable AuthenticationCodes code) {
        return code == null ? loginUrl : loginUrl + "?status=" + code;
    }

    @NotNull
    public String getLoginRedirectUrl(@NotNull final String redirectToUrl) {
        return loginRedirectUrl + URLEncoder.encode(redirectToUrl, StandardCharsets.UTF_8);
    }

    @NotNull
    public String getOrderHomepageUrl() {
        return getOrderHomepageUrl(null);
    }
    @NotNull
    public String getOrderHomepageUrl(@Nullable OrderWorkflowErrorCode errorCode) {
        return errorCode == null ? orderHomepageUrl : orderHomepageUrl + "?error=" + errorCode;
    }

    @NotNull
    public String getPasswordResetUrl(UUID pwResetId) {
        return passwordResetUrl + pwResetId;
    }
}
