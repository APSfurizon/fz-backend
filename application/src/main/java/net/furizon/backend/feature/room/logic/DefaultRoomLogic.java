package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("roomLogic-default")
public class DefaultRoomLogic implements RoomLogic {
}
