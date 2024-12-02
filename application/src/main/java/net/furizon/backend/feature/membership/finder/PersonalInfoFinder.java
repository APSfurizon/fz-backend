package net.furizon.backend.feature.membership.finder;

import net.furizon.backend.feature.membership.dto.PersonalUserInformation;
import org.jetbrains.annotations.Nullable;

public interface PersonalInfoFinder {
    @Nullable
    PersonalUserInformation findByUserId(long userId);
}
