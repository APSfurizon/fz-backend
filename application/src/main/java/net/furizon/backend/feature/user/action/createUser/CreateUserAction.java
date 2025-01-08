package net.furizon.backend.feature.user.action.createUser;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CreateUserAction {
    @NotNull
    User invoke(@NotNull String fursonaName, @Nullable String locale);
}
