package net.furizon.backend.infrastructure.security;

public enum GeneralResponseCodes {
    USER_IS_NOT_ADMIN,
    USER_NOT_FOUND,
    ORDER_ALREADY_BOUGHT, //We expected that the user had no order, but instead he has at least one
    ROLE_NOT_FOUND,
    EVENT_NOT_FOUND,
    ORDER_NOT_FOUND, //Unable to find the specified order
    ORDER_NOT_PAID, //An order is not in a paid status
    //An order is found with payments in CREATED or PENDING state. Only confirmed and canceled states are allowed
    ORDER_PAYMENTS_STILL_PENDING,
    //An order is found with refunds in CREATED, TRANSIT or EXTERNAL states. Only confirmed and canceled are allowed
    ORDER_REFUNDS_STILL_PENDING,
    USER_HAS_DAILY_TICKET, //An order is of the type daily, thus it cannot attend the event in a room
    EDIT_TIMEFRAME_ENDED,
}
