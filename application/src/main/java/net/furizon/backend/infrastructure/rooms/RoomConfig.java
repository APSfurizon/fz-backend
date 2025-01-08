package net.furizon.backend.infrastructure.rooms;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Data
@ConfigurationProperties(prefix = "room")
public class RoomConfig {
    @Nullable
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final OffsetDateTime roomChangesEndTime;
}
