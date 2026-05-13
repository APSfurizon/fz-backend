package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public interface PersonalInfoFinder {
    @NotNull Map<Long, Pair<String, String>> getFullNameByUserIds(@NotNull Collection<Long> userIds);

    @Nullable
    PersonalUserInformation findByUserId(long userId);

    @NotNull
    Map<Long, LocalDate> findAllUserIdsByExpiredId();
}
