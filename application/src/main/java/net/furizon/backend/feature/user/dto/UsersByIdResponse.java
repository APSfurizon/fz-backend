package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Data
public class UsersByIdResponse {
    @NotNull
    private final Collection<UserDisplayData> users;
}
