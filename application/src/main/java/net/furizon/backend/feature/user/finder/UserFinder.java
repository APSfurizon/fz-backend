package net.furizon.backend.feature.user.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.objects.SearchUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface UserFinder {
    @Nullable
    User findById(long userId);

    @NotNull
    List<SearchUser> searchUserInCurrentEvent(
            @NotNull String fursonaName,
            @NotNull Event event,
            boolean filterRoom,
            boolean filterPaid,
            @Nullable Short filterMembershipCardForYear
    );

    @Nullable
    UserEmailData getMailDataForUser(long userId);

    @Nullable
    UserDisplayData getDisplayUser(long userId, @NotNull Event event);

    @NotNull
    List<UserDisplayData> getDisplayUserByIds(Set<Long> ids, @NotNull Event event);
}
