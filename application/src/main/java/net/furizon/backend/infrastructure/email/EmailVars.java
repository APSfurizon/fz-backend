package net.furizon.backend.infrastructure.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmailVars {
    FURSONA_NAME("otherfursonaName"),
    ROOM_OWNER_FURSONA_NAME("ownerFursonaName"),
    ROOM_NAME("roomName"),
    ROOM_TYPE_NAME("roomTypeName"),
    EXCHANGE_ACTION_TEXT("exchangeActionText"),
    EXCHANGE_LINK("exchangeLink"),
    SANITY_CHECK_REASON("sanityCheckReason"),
    EVENT_NAME("eventName"),
    ORDER_CODE("orderCode"),
    ORDER_OWNER_ID("orderOwnerId"),
    ORDER_PREV_OWNER_ID("orderPrevOwnerId"),
    DUPLICATE_ORDER_CODE("duplicateOrderCode"),
    MEMBERSHIP_CARD_ID("cardId"),
    MEMBERSHIP_CARD_ID_IN_YEAR("cardIdInYear"),
    ;

    @Getter
    private final String name;
}
