package net.furizon.backend.feature.user.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class SearchUsersResponse {
    @NotNull
    private final List<SearchUser> users;

    public record SearchUser(long userId, @NotNull String fursonaName, @Nullable String propicUrl){}
}
