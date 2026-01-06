package net.furizon.backend.feature.room.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class RoomInfo {
    private final long roomId;

    @Nullable
    private final String roomName;

    private final UserDisplayData roomOwner;
    @Builder.Default
    private boolean userIsOwner = false;

    @Builder.Default
    private boolean confirmationSupported = false;
    @Builder.Default
    private boolean canConfirm = false;
    @Builder.Default
    private boolean unconfirmationSupported = false;
    @Builder.Default
    private boolean canUnconfirm = false;
    private final boolean confirmed;

    private final boolean showInNosecount;

    private final long eventId;

    @NotNull
    private final RoomData roomData;

    @Builder.Default
    private boolean canInvite = false;

    @NotNull
    @Builder.Default
    private final ExtraDays extraDays = ExtraDays.NONE;

    @NotNull
    @Builder.Default
    private final Board board = Board.NONE;

    @Nullable
    @Builder.Default
    private List<RoomGuestResponse> guests = null;

    @Nullable
    private final LocalDate checkinDate;
    @Nullable
    private final LocalDate checkoutDate;


    public static class RoomInfoBuilder {
        public RoomInfoBuilder checkinoutDates(@NotNull Event event, @Nullable ExtraDays extraDays) {
            if (extraDays == null) {
                if (!this.extraDays$set) {
                    throw new IllegalArgumentException("extraDays must not be null or they must be previously set!");
                }
                extraDays = this.extraDays$value;
            } else {
                this.extraDays$set = true;
                this.extraDays$value = extraDays;
            }
            checkinDate = computeCheckin(event, extraDays);
            checkoutDate = computeCheckout(event, extraDays);
            return this;
        }
    }

    public static @Nullable LocalDate computeCheckin(@NotNull Event event, @NotNull ExtraDays extraDays) {
        OffsetDateTime from = event.getCorrectDateFrom();
        if (from != null) {
            return (extraDays.isEarly() ? from.minusDays(ExtraDays.EARLY_DAYS_NO) : from).toLocalDate();
        }
        return null;
    }
    public static @Nullable LocalDate computeCheckout(@NotNull Event event, @NotNull ExtraDays extraDays) {
        OffsetDateTime to = event.getCorrectDateTo();
        if (to != null) {
            return (extraDays.isLate() ? to.plusDays(ExtraDays.LATE_DAYS_NO) : to).toLocalDate();
        }
        return null;
    }
}
