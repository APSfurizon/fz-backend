package net.furizon.backend.infrastructure.rooms;

import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import org.jetbrains.annotations.NotNull;

public class RoomEmailTexts {
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

    public static final TranslatableValue SC_NO_OWNER = TranslatableValue.ofEmail(
            "mail.sanity_check.error.no_owner");
    public static final TranslatableValue SC_TOO_MANY_MEMBERS = TranslatableValue.ofEmail(
            "mail.sanity_check.error.too_many_members");
    public static final TranslatableValue SC_OWNER_HAS_NO_ORDER = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_has_no_order");
    public static final TranslatableValue SC_OWNER_HAS_NOT_BOUGHT_ROOM = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_has_not_bought_room");
    public static final TranslatableValue SC_OWNER_HAS_DAILY_TICKET = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_has_daily_ticket");
    public static final TranslatableValue SC_OWNER_IN_TOO_MANY_ROOMS = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_in_too_many_rooms");
    public static final TranslatableValue SC_OWNER_ORDER_INVALID_STATUS = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_order_invalid_status");
    public static final TranslatableValue SC_OWNER_NOT_IN_ROOM = TranslatableValue.ofEmail(
            "mail.sanity_check.error.owner_not_in_room");
    public static final TranslatableValue SC_USER_ORDER_INVALID_ORDER_STATUS = TranslatableValue.ofEmail(
            "mail.sanity_check.error.user_order_invalid_order_status");
    public static final TranslatableValue SC_USER_IN_TOO_MANY_ROOMS = TranslatableValue.ofEmail(
            "mail.sanity_check.error.user_in_too_many_rooms");
    public static final TranslatableValue SC_USER_HAS_DAILY_TICKET = TranslatableValue.ofEmail(
            "mail.sanity_check.error.user_has_daily_ticket");
    public static final TranslatableValue SC_USER_HAS_NO_ORDER = TranslatableValue.ofEmail(
            "mail.sanity_check.error.user_has_no_order");

    public static final TranslatableValue TRANSFER_FULL_ORDER = TranslatableValue.ofEmail("order_exchange");
    public static final TranslatableValue TRANSFER_ROOM = TranslatableValue.ofEmail("room_transfer");
    public static final TranslatableValue EXCHANGE_ROOM = TranslatableValue.ofEmail("room_exchange");


    public static final TranslatableValue getActionText(@NotNull ExchangeAction action, boolean destHasRoom) {
        return switch (action) {
            case TRASFER_EXCHANGE_ROOM -> TRANSFER_FULL_ORDER;
            case TRASFER_FULL_ORDER -> destHasRoom ? EXCHANGE_ROOM : TRANSFER_ROOM;
        };
    }

    public static final String LANG_PRETIX = "en";
}
