package net.furizon.backend.feature.pretix.objects.checkins.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CheckinHistoryOrder {
    DATE_TIME("datetime"),
    CREATED_AT("created"),
    CREATION_ORDER("id");

    @Getter
    private final String pretixField;
}
