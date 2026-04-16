package net.furizon.backend.feature.pretix.objects.checkins.action.invalidateCheckin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PretixInvalidateCheckinAction {
    boolean invoke(@NotNull String organizer,
                   @NotNull String nonce,
                   @NotNull List<Long> checkinListIds,
                   @Nullable String explanation);
}
