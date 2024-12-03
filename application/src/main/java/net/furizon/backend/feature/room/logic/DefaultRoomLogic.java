package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-default", matchIfMissing = true)
public class DefaultRoomLogic implements RoomLogic {
}
