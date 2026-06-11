package net.furizon.backend.infrastructure.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum StoreMethod {
    DISK(0),
    S3_LOCAL(1),
    S3_REMOTE(2);

    @Getter
    private final int methodId;

    @NotNull
    public String getUrl(@NotNull String url) {
        return FrontendConfig.CONFIG.getStaticFileUrl(url, this);
    }

    private static final Map<Integer, StoreMethod> STORE_METHODS = Arrays
            .stream(StoreMethod.values())
            .collect(Collectors.toMap(StoreMethod::getMethodId, Function.identity()));

    @Nullable
    public static StoreMethod get(final int value) {
        return STORE_METHODS.get(value);
    }
}
