package net.furizon.backend.feature.user.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.dto.UserDisplayDataResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UserFinder {
    @Nullable
    User findById(long userId);

    @NotNull
    List<SearchUsersResponse.SearchUser> searchUserInCurrentEvent(
            @NotNull String fursonaName,
            @NotNull Event event,
            boolean filterRoom
    );

    @Nullable
    UserDisplayDataResponse getDisplayUser(long userId, @NotNull Event event);
}
