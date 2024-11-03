package net.furizon.backend.feature.user;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@Builder
public class User {
    private final long id;

    @Nullable
    private final String fursonaName;
}
