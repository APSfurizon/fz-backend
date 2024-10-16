package net.furizon.backend.feature.user.finder;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UserFinder {
    @NotNull
    List<User> getAllUsers();

    @Nullable
    User findUserById(long id);
}
