package net.furizon.backend.infrastructure.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmailVars {
    FURSUIT_NAME("fursuitName"),
    FURSONA_NAME("fursonaName"),
    OTHER_FURSONA_NAME("otherfursonaName"),
    ROOM_OWNER_FURSONA_NAME("ownerFursonaName"),
    ROOM_NAME("roomName"),
    ROOM_TYPE_NAME("roomTypeName"),
    ROOM_CAPACITY("roomCapacity"),
    ROOM_CURRENT_GUEST_NO("roomGuestNo"),
    EXCHANGE_ACTION_TEXT("exchangeActionText"),
    SANITY_CHECK_REASON("sanityCheckReason"),
    EVENT_NAME("eventName"),
    ORDER_CODE("orderCode"),
    OTHER_ORDER_CODE("otherOrderCode"),
    ORDER_OWNER_ID("orderOwnerId"),
    ORDER_PREV_OWNER_ID("orderPrevOwnerId"),
    DUPLICATE_ORDER_CODE("duplicateOrderCode"),
    MEMBERSHIP_CARD_ID("cardId"),
    MEMBERSHIP_CARD_ID_IN_YEAR("cardIdInYear"),
    REFUND_MONEY("refundMoney"),
    LINK("link"),
    DEADLINE("deadline"),
    ;

    @Getter
    private final String name;
}
