package net.furizon.backend.feature.user;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@Builder
public class User {
    final long id;

    @Nullable
    final String firstname;

    @Nullable
    final String lastname;
}
