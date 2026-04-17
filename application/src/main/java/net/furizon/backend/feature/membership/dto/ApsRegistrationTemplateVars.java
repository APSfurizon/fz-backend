package net.furizon.backend.feature.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApsRegistrationTemplateVars {
    CARD_NO("cardNo"),
    FULL_NAME("fullname"),
    FISCAL_CODE("fiscalCode"),
    BIRTH_CITY("birthCity"),
    BIRTH_DAY("birthDay"),
    CITY("city"),
    REGION("region"),
    ADDRESS("address"),
    ZIP_CODE("zipCode"),
    EMAIL("email"),
    PHONE_NO("phoneNumber"),
    TODAY_DATE("todayDate");

    @Getter
    private final @NotNull String varName;

    @Override
    public String toString() {
        return varName;
    }
}
