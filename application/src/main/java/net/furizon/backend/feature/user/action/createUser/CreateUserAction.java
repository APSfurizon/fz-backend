package net.furizon.backend.feature.user.action.createUser;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;

public interface CreateUserAction {
    @NotNull
    User invoke(@NotNull String fursonaName);
}
