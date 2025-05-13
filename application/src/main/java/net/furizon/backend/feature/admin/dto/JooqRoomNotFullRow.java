package net.furizon.backend.feature.admin.dto;

import lombok.Data;

@Data
public class JooqRoomNotFullRow {
    private final long userId;
    private final short roomCount;
    private final short roomCapacity;
    private final long roomPretixItemId;
}
