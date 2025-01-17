package net.furizon.backend.infrastructure.rooms;

import net.furizon.backend.feature.room.dto.ExchangeAction;
import org.jetbrains.annotations.NotNull;

public class RoomEmailTexts {
    public static final String SUBJECT_ROOM_PROBLEM = "There's a problem with your room reservation";
    public static final String SUBJECT_ROOM_UPDATE = "There's an update regarding your room reservation";
    public static final String SUBJECT_EXCHANGE_FULLORDER_REFUND_FAILED = "Full order exchange REFUND FAILED!!";
    public static final String SUBJECT_EXCHANGE_ROOM_MONEY_FAILED = "Room exchange REFUND or PAYMENT FAILED!!";


    public static final String TEMPLATE_ROOM_HAS_NEW_OWNER = "room_has_new_owner.jte";
    public static final String TEMPLATE_ROOM_WAS_UPGRADED = "room_was_upgraded.jte";
    public static final String TEMPLATE_ROOM_CONFIRMED = "room_confirmed.jte";
    public static final String TEMPLATE_ROOM_UNCONFIRMED = "room_unconfirmed.jte";
    public static final String TEMPLATE_ROOM_DELETED = "room_deleted.jte";
    public static final String TEMPLATE_EXCHANGE_INITIALIZED = "exchange_initialized.jte";
    public static final String TEMPLATE_EXCHANGE_FULLORDER_REFUND_FAILED = "exchange_fullorder_failed_refund.jte";
    public static final String TEMPLATE_EXCHANGE_ROOM_MONEY_FAILED = "exchange_room_failed_money.jte";
    public static final String TEMPLATE_EXCHANGE_COMPLETED = "exchange_completed.jte";
    public static final String TEMPLATE_INVITE_ACCEPTED = "invite_accepted.jte";
    public static final String TEMPLATE_INVITE_REFUSE = "invite_refused.jte";
    public static final String TEMPLATE_KICKED_FROM_ROOM = "room_kicked_from.jte";
    public static final String TEMPLATE_USER_LEFT_ROOM = "room_user_left.jte";
    public static final String TEMPLATE_SANITY_CHECK_DELETED = "sanity_check_deleted.jte";
    public static final String TEMPLATE_SANITY_CHECK_KICK_USER = "sanity_check_kick_user.jte";
    public static final String TEMPLATE_SANITY_CHECK_KICK_OWNER = "sanity_check_kick_owner.jte";


    public static final String SC_NO_OWNER = "No owner was found in your room!";
    public static final String SC_TOO_MANY_MEMBERS = "There are too many members in your room!";
    public static final String SC_OWNER_HAS_NO_ORDER = "Room's owner has no registered order!";
    public static final String SC_OWNER_HAS_NOT_BOUGHT_ROOM = "Room's owner has not bought a room!";
    public static final String SC_OWNER_HAS_DAILY_TICKET = "Room's owner has daily ticket!";
    public static final String SC_OWNER_IN_TOO_MANY_ROOMS = "Room's owner is in too many rooms!";
    public static final String SC_OWNER_ORDER_INVALID_STATUS = "Room's owner order is not in "
            + "'Paid' or 'Pending' status!";
    public static final String SC_OWNER_NOT_IN_ROOM = "Room's owner is not a room member!";
    public static final String SC_USER_ORDER_INVALID_ORDER_STATUS = "User order is not in 'Paid' or 'Pending' status!";
    public static final String SC_USER_IN_TOO_MANY_ROOMS = "User is in too many rooms!";
    public static final String SC_USER_HAS_DAILY_TICKET = "User has daily ticket!";
    public static final String SC_USER_HAS_NO_ORDER = "User has no registered order!";


    public static final String TRANSFER_FULL_ORDER = "full order transfer";
    public static final String TRANSFER_ROOM = "room transfer";
    public static final String EXCHANGE_ROOM = "room exchange";


    public static final String getActionText(@NotNull ExchangeAction action, boolean destHasRoom) {
        return switch (action) {
            case TRASFER_EXCHANGE_ROOM -> TRANSFER_FULL_ORDER;
            case TRASFER_FULL_ORDER -> destHasRoom ? EXCHANGE_ROOM : TRANSFER_ROOM;
        };
    }

    public static final String LANG_PRETIX = "en";
}
