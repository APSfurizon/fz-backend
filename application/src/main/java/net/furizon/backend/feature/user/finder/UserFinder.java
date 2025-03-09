package net.furizon.backend.feature.user.finder;

import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.UserAdminViewDisplay;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.objects.SearchUserResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface UserFinder {
    @Nullable
    User findById(long userId);

    @NotNull List<UserEmailData> getMailDataForUsersWithNoPropic(@NotNull Event event);

    @NotNull
    List<SearchUserResult> searchUserInCurrentEvent(
            @NotNull String inputQuery,
            boolean isAdminSearch,
            @NotNull Event event,
            boolean filterRoom,
            boolean filterPaid,
            boolean filerNotMadeAnOrder,
            @Nullable Short filterMembershipCardForYear,
            @Nullable Boolean filterBanStatus
    );

    @Nullable
    UserEmailData getMailDataForUser(long userId);
    @NotNull
    List<UserEmailData> getMailDataForUsers(@NotNull List<Long> userIds);

    @Nullable
    UserDisplayData getDisplayUser(long userId, @NotNull Event event);

    @NotNull
    List<UserDisplayData> getDisplayUserByIds(Set<Long> ids, @NotNull Event event);

    @Nullable
    UserAdminViewDisplay getUserAdminViewDisplay(long userId, @NotNull Event event);
}
