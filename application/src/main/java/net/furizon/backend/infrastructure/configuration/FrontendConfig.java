package net.furizon.backend.infrastructure.configuration;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URISyntaxException;
import java.util.UUID;

@Data
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    @Getter(AccessLevel.NONE)
    @NotNull private String loginUrl;

    @Getter(AccessLevel.NONE)
    @NotNull private String passwordResetUrl;

    @Getter(AccessLevel.NONE)
    @NotNull private String confirmEmailUrl;

    @NotNull private String reservationPageUrl;
    @NotNull private String badgePageUrl;
    @NotNull private String roomPageUrl;
    @NotNull private String userPageUrl;

    @NotNull private StaticFiles staticFiles;
    @Data
    public static class StaticFiles {
        @NotNull private String localDiskUrl;
        @NotNull private String s3LocalUrl;
        @NotNull private String s3RemoteUrl;
    }

    @NotNull
    public String getLoginUrl() {
        return getLoginUrl(null);
    }
    @NotNull
    public String getLoginUrl(@Nullable AuthenticationCodes code) {
        try {
            URIBuilder ucb = new URIBuilder(loginUrl);
            if (code != null) {
                ucb.addParameter("status", code.name());
            }
            return ucb.build().toString();
        } catch (URISyntaxException use) {
            return loginUrl;
        }
    }

    @NotNull
    public String getPasswordResetUrl(@NotNull UUID pwResetId) {
        return passwordResetUrl + pwResetId;
    }

    @NotNull
    public String getConfirmEmailUrl(@NotNull UUID confirmUrlId) {
        return confirmEmailUrl + confirmUrlId;
    }

    @NotNull
    public String getStaticFileUrl(@NotNull String file, @NotNull StoreMethod storeMethod) {
        return switch (storeMethod) {
            case DISK -> staticFiles.localDiskUrl + file;
            case S3_LOCAL -> staticFiles.s3LocalUrl + file;
            case S3_REMOTE -> staticFiles.s3RemoteUrl + file;
        };
    }


    public static FrontendConfig CONFIG;
    @PostConstruct
    public void init() {
        CONFIG = this;
    }
}
