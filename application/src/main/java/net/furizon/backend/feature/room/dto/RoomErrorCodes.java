package net.furizon.backend.feature.room.dto;

public enum RoomErrorCodes {
    ROOM_NOT_FOUND, //No room is found
    ROOM_FULL, //The room is full and no other users are allowed to enter
    ROOM_CANNOT_BE_CONFIRMED, //The room cannot be confirmed
    ROOM_CANNOT_BE_UNCONFIRMED, //The room cannot be unconfirmed
    ROOM_ALREADY_CONFIRMED, //Room was expected to NOT be confirmed, but instead it was
    ROOM_NOT_CONFIRMED, //The room is not confirmed
    GUEST_NOT_FOUND, //Unable to find the specified guest
    GUEST_ALREADY_CONFIRMED, //We expected that the guest had not confirmed yet, but instead it has
    GUEST_NOT_CONFIRMED, //Guest hasn't confirmed yet
    ORDER_NOT_FOUND, //Unable to find the specified order
    ORDER_ALREADY_BOUGHT, //We expected that the user had no order, but instead he has at least one
    ORDER_NOT_PAID, //An order is not in a paid status
    ORDER_PAYMENTS_STILL_PENDING, //An order is found with payments in CREATED or PENDING state. Only confirmed and canceled states are allowed
    ORDER_REFUNDS_STILL_PENDING, //An order is found with refunds in CREATED, TRANSIT or EXTERNAL states. Only confirmed and canceled states are allowed
    EDIT_TIMEFRAME_ENDED, //You tried editing a room, but the allowed editing time is passed
    BUY_ROOM_NEW_ROOM_COSTS_LESS, //An user has tried purchasing a room which costs less than what he originally paid
    BUY_ROOM_NEW_ROOM_LOW_CAPACITY, //An user has tried purchasing a room which less capacity than the number members who are already in the room
    BUY_ROOM_NEW_ROOM_QUOTA_ENDED, //An user has tried purchasing a room which quota has ended
    BUY_ROOM_ERROR_UPDATING_POSITION, //An error occurred while actually adding the room to the user's order
    USER_IS_NOT_ADMIN, //TODO use the same error code as other admin checks! [ADMIN_CHECK]
    USER_HAS_DAILY_TICKET, //An order is of the type daily, thus it cannot attend the event in a room
    USER_HAS_PURCHASED_A_ROOM, //The user was expected to NOT have purchased a room from pretix, but instead it has
    USER_HAS_NOT_PURCHASED_A_ROOM, //The user hasn't bought a room from pretix yet
    USER_IS_OWNER_OF_ROOM, //The specified user is the owner of the room and the operation cannot be performed on him
    USER_OWNS_A_ROOM, //The user is expected to NOT have created a room, but instead it has
    USER_DOES_NOT_OWN_A_ROOM, //The user doesn't have created a room yet
    USER_ALREADY_IS_IN_A_ROOM, //The user is already part (and his guest entry is confirmed) of the specified room
    USER_ALREADY_INVITED_TO_ROOM, //The user is already invited to the specified room
    EXCHANGE_NOT_FOUND, //Unable to find the exchange confirmation status from the gived exchangeId
    EXCHANGE_NOT_FULLY_CONFIRMED, //Tried confirming the exchange, but both users have not confirmed yet
    EXCHANGE_STILL_PENDING, //The user has tried starting a new exchange, but he still has one active
}
