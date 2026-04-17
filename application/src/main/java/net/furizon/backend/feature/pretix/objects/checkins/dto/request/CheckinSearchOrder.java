package net.furizon.backend.feature.pretix.objects.checkins.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CheckinSearchOrder {
    ORDER_CODE("order__code,positionid"),
    ORDER_DATE("order__datetime,positionid"),
    ATTENDEE_NAME("attendee_name,positionid"),
    CHECKIN_DATE("last_checked_in,positionid");

    @Getter
    private final String pretixField;
}
