package net.furizon.backend.feature.admin.dto;

import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class HotelExportRow {
    @NotNull private final String roomTypeName;
    private boolean roomConfirmed;
    private final long roomId;
    @NotNull private final String firstName;
    @NotNull private final String lastName;
    @Past @NotNull private final LocalDate birthday;
    @NotNull private final String birthLocation;
    @NotNull private final String fullResidenceAddress;
    @NotNull private final String email;
    @NotNull private final String phone;
    private final long userId;
    @NotNull private final String userName;
    @NotNull private final String orderCode;
    @NotNull private final String roomOwnerOrderCode;
    @NotNull private final ExtraDays extraDays;
    @NotNull private final String idType;
    @NotNull private final String idNumber;
    @NotNull private final OffsetDateTime idExpiry;
    @NotNull private final String idIssuer;

    private boolean requiresAttention;
    @Nullable private String comment;
}
