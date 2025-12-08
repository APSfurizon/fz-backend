package net.furizon.backend.infrastructure.http.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public interface HttpConfig {
    @NotNull
    String getBaseUrl();

    @NotNull
    default MultiValueMap<@NotNull String, @NotNull String> headers() {
        return new LinkedMultiValueMap<>();
    }

    @NotNull
    default String getBasePath() {
        return "";
    }

    @NotNull HttpHeaders httpHeaders();
}
