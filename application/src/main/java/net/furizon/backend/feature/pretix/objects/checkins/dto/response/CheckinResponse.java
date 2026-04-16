package net.furizon.backend.feature.pretix.objects.checkins.dto.response;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.badge.dto.PrintedBadgeLevel;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.pretix.objects.checkins.PortaBadgeLevel;
import net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets.Gadget;
import net.furizon.backend.feature.pretix.objects.checkins.dto.pretix.RedeemCheckinResponse;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.jooq.generated.enums.IdType;
import net.furizon.jooq.generated.enums.ShirtSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CheckinResponse {
    @NotNull
    private final String checkinNonce;

    //Checkin response

    @NotNull
    private final RedeemCheckinResponse.Status status;

    @Nullable
    private final String localizedErrorReason;

    @Nullable
    private final String optionalErrorMessage;

    //Various info

    @NotNull
    private final UserDisplayData user;

    @NotNull
    private final String orderCode;

    private final long orderSerial;

    @NotNull
    private final List<MembershipCard> cardsForEvent;

    @NotNull
    private final List<FursuitData> fursuits;

    private final boolean hasFursuitBadge;

    private final boolean isDailyTicket;

    private final boolean isStaffer;

    @NotNull
    private final Sponsorship sponsorship;

    @NotNull
    private final PrintedBadgeLevel lanyardType;

    @NotNull
    private final PortaBadgeLevel portaBadgeType;

    @Nullable
    private final RoomInfo roomInfo;

    @NotNull
    private final List<Gadget> gadgets;

    // Personal user information

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    @NotNull
    private final String birthCity;
    @Nullable
    private final String birthRegion;
    @NotNull
    private final String birthCountry;
    @NotNull
    private final LocalDate birthday;

    //@Nullable
    //private final String allergies;

    @Nullable
    private final String fiscalCode;

    @NotNull
    private final ShirtSize shirtSize;

    @NotNull
    private final IdType idType;
    @NotNull
    private final String idNumber;
    @NotNull
    private final String idIssuer;
    @NotNull
    private final LocalDate idExpiry;

    // Extra texts

    private final boolean requiresAttention;

    @Nullable
    private final String customerNote;
    @Nullable
    private final String internalNote;

    @Nullable
    private final List<String> checkinTexts;


}
