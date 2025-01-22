package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class SearchUsersResponse {
    @NotNull
    private final List<SearchUserResult> users;
}
