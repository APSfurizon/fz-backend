package net.furizon.backend.feature.room.dto.request;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Data
@Builder
public class TransferOrderRequest {
    @NotNull private final String orderCode;

    @NotNull private final Collection<Long> ticketItemIds;

    @NotNull private final Collection<Long> membershipCardItemIds;
    private final boolean membershipCardNeededForNewUser;

    private final long userIdQuestionId;
    private final long newUserId;

    @NotNull
    private final String newEmail;
    @Nullable
    private final String name;
    @Nullable
    private final String street;
    @Nullable
    private final String zipcode;
    @Nullable
    private final String city;
    @Nullable
    private final String state;
    @Nullable
    private final String country;


    @Nullable
    private final String cancellationComment;
    @Nullable
    private final String manualPaymentComment;
    @Nullable
    private final String manualRefundComment;
}
