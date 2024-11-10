package net.furizon.backend.feature.authentication.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonalUserInformation {
    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String firstName;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String lastName;

    @NotNull
    @PastOrPresent
    private final LocalDate birthday;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String birthCity;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String birthProvince;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String birthCountry;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String currentAddress;

    @NotNull
    @NotEmpty
    @Size(min = 2)
    private final String phoneNumber;

    @Nullable
    private final String socialSecurityNumber;
}
