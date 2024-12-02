package net.furizon.backend.feature.user.finder;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.Nullable;

public interface UserFinder {
    @Nullable
    User findById(long id);
}
