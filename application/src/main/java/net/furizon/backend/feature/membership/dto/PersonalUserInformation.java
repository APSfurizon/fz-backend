package net.furizon.backend.feature.membership.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import net.furizon.jooq.generated.enums.IdType;
import net.furizon.jooq.generated.enums.ShirtSize;

import java.time.LocalDate;

// Italian stuff needed by law for associations.
// If for some reason you have to adopt this codebase in your country,
// you probably must adapt or change the membership code
@Data
@Builder
public class PersonalUserInformation {
    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String firstName;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String lastName;

    // Italian person "id". For non italian ppl leave this to null
    // If null, in documents this should appear as ""
    @Nullable
    @Size(min = 16, max = 16)
    @Pattern(regexp = "^[A-Za-z]{6}\\d{2}[A-Za-z]\\d{2}[A-Za-z]\\d{3}[A-Za-z]$")
    private final String fiscalCode;

    @NotNull
    @Pattern(regexp = "^[MmFf]$")
    private final String sex;

    @Nullable
    @Size(min = 4, max = 64)
    private final String gender;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String birthCity;

    @Nullable
    @Size(min = 2)
    private final String birthRegion;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 2)
    private final String birthCountry;

    @NotNull
    @Past
    private final LocalDate birthday;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String residenceAddress;

    @NotNull
    @NotEmpty
    @Size(max = 16)
    private final String residenceZipCode;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String residenceCity;

    @Nullable
    @Size(min = 2)
    private final String residenceRegion;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 2)
    private final String residenceCountry;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String phoneNumber;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String prefixPhoneNumber;

    @Nullable
    @Size(max = 1000)
    private final String allergies;

    @Nullable
    @Size(max = 1000)
    private final String note;

    @NotNull
    @Pattern(regexp = "^@?[a-zA-Z0-9_]{5,32}$")
    private final String telegramUsername;

    //Those are intended to be primitive types
    @NotNull
    private final IdType idType;

    @NotNull
    @NotEmpty
    private final String idNumber;

    @NotNull
    @NotEmpty
    private final String idIssuer;

    @NotNull
    private final LocalDate idExpiry;

    @NotNull
    private final ShirtSize shirtSize;

    //These are intended to be primitive types
    private final long id;
    private final long lastUpdatedEventId;
    private final long userId;
}
