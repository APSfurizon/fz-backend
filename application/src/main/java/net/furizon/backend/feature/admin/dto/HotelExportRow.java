package net.furizon.backend.feature.admin.dto;

import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Data
@Builder
public class HotelExportRow {
    @NotNull private final String roomTypeName;
    private final long roomId;
    @NotNull private final String firstName;
    @NotNull private final String lastName;
    @Past @NotNull private final LocalDate birthday;
    @NotNull private final String fullResidenceAddress;
    @NotNull private final String email;
    @NotNull private final String phone;
    private final long userId;
    @NotNull private final String orderCode;
    @NotNull private final String roomOwnerOrderCode;

    private boolean requiresAttention;
    @Nullable private String comment;
}
