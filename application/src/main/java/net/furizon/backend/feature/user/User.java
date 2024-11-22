package net.furizon.backend.feature.user;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@Builder
public class User {
    private final long id;

    @NotNull
    private final String fursonaName;

    @Nullable
    private final String locale;

    private final long propicId;
}
