package net.furizon.backend.feature.pretix.objects.checkins.action.redeemCheckin;

import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.RedeemCheckinResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PretixRedeemCheckinAction {
    @Nullable RedeemCheckinResponse invoke(@NotNull String secret,
                                           @NotNull String organizer,
                                           @NotNull String nonce,
                                           @NotNull List<Long> checkinListIds);
}
