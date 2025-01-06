package net.furizon.backend.infrastructure.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmailVars {
    OWNER_FURSONA_NAME("ownerFursonaName"),
    FURSONA_NAME("otherfursonaName"),
    ROOM_NAME("roomName"),
    ROOM_TYPE_NAME("roomTypeName"),
    EXCHANGE_ACTION_TEXT("exchangeActionText"),
    EXCHANGE_LINK("exchangeLink"),
    SANITY_CHECK_REASON("sanityCheckReason"),
    EVENT_NAME("eventName"),
    ORDER_CODE("orderCode"),
    DUPLICATE_ORDER_CODE("duplicateOrderCode"),
    ;

    @Getter
    private final String name;
}
