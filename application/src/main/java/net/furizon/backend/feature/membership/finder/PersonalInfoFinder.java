package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Map;

public interface PersonalInfoFinder {
    @Nullable
    PersonalUserInformation findByUserId(long userId);

    @NotNull
    Map<Long, LocalDate> findAllUserIdsByExpiredId();
}
