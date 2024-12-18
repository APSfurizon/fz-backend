package net.furizon.backend.feature.user.dto;

import lombok.Data;
import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class UsersByIdResponse {
    @NotNull
    private final List<User> users;
}
