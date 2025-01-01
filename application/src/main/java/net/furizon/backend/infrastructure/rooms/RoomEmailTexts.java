package net.furizon.backend.infrastructure.rooms;

import static net.furizon.backend.infrastructure.email.EmailVars.EXCHANGE_ACTION_TEXT;
import static net.furizon.backend.infrastructure.email.EmailVars.EXCHANGE_LINK;
import static net.furizon.backend.infrastructure.email.EmailVars.FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.OWNER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_TYPE_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.SANITY_CHECK_REASON;

public class RoomEmailTexts {
    public static final String SUBJECT_ROOM_PROBLEM = "There's a problem with your room reservation";
    public static final String SUBJECT_ROOM_UPDATE = "There's an update regarding your room reservation";


    public static final String TITLE_RESERVATION_UPDATED = "Your accommodation got an update!";
    public static final String TITLE_ROOM_UPDATED = "Your room got an update!";
    public static final String TITLE_ROOM_FAILED_SC = "Your accommodation has failed our sanity checks.";


    public static final String BODY_ROOM_HAS_NEW_OWNER = "You're receiving this email because the <b>owner of the room</b> you're into for the upcoming event <b>has changed</b>! The new owner is " + OWNER_FURSONA_NAME + "!";
    public static final String BODY_ROOM_WAS_UPGRADED = "You're receiving this email because the owner of the room has <b>bought an upgrade for the room</b>! Your new accommodation type is " + ROOM_TYPE_NAME + "!";
    public static final String BODY_ROOM_CONFIRMED = "You're receiving this email because your " + ROOM_TYPE_NAME + " room <b>has been confirmed</b>!";
    public static final String BODY_ROOM_UNCONFIRMED = "You're receiving this email because your " + ROOM_TYPE_NAME + " room <b>has been unconfirmed</b> by the room's owner!";
    public static final String BODY_ROOM_DELETED = "You're receiving this email because your " + ROOM_TYPE_NAME + " room <b>has been deleted</b> by the room's owner!";
    public static final String BODY_EXCHANGE_INITIALIZED = "You're receiving this email because a " + EXCHANGE_ACTION_TEXT + " has been initialized between " + OWNER_FURSONA_NAME + " and " + FURSONA_NAME + ". To watch <b>more details</b> and <b>accept</b> or <b>refuse</b>, visit the following link: " + EXCHANGE_LINK;
    public static final String BODY_INVITE_ACCEPTED = "You're receiving this email because " + FURSONA_NAME + " has <b>accepted your room invitation</b>!";
    public static final String BODY_INVITE_REFUSE = "You're receiving this email because " + FURSONA_NAME + " has <b>refused your room invitation</b>!";
    public static final String BODY_KICKED_FROM_ROOM = "You're receiving this email because the room's owner " + OWNER_FURSONA_NAME + " <b>has kicked you</b> out from room " + ROOM_NAME;
    public static final String BODY_USER_LEFT_ROOM = "You're receiving this email because " + FURSONA_NAME + " <b>has left</b> your room!";
    public static final String BODY_SANITY_CHECK_DELETED = "You're receiving this email because your room " + ROOM_NAME + " <b>was automatically deleted</b> by our sanity check system for the following reason: " + SANITY_CHECK_REASON;
    public static final String BODY_SANITY_CHECK_KICK_USER = "You're receiving this email because <b>you've been automatically kicked</b> from room " + ROOM_NAME + " by our sanity check system for the following reason: " + SANITY_CHECK_REASON;
    public static final String BODY_SANITY_CHECK_KICK_OWNER = "You're receiving this email because " + FURSONA_NAME + " <b>has been automatically kicked</b> from your room " + ROOM_NAME + " by our sanity check system for the following reason: " + SANITY_CHECK_REASON;


    public static final String SC_NO_OWNER = "No owner was found in your room!";
    public static final String SC_TOO_MANY_MEMBERS = "There are too many members in your room!";
    public static final String SC_OWNER_HAS_NO_ORDER = "Room's owner has no registered order!";
    public static final String SC_OWNER_HAS_NOT_BOUGHT_ROOM = "Room's owner has not bought a room!";
    public static final String SC_OWNER_HAS_DAILY_TICKET = "Room's owner has daily ticket!";
    public static final String SC_OWNER_IN_TOO_MANY_ROOMS = "Room's owner is in too many rooms!";
    public static final String SC_OWNER_ORDER_INVALID_ORDER_STATUS = "Room's owner order is not in 'Paid' or 'Pending' status!";
    public static final String SC_OWNER_NOT_IN_ROOM = "Room's owner is not a room member!";
    public static final String SC_USER_ORDER_INVALID_ORDER_STATUS = "User order is not in 'Paid' or 'Pending' status!";
    public static final String SC_USER_IN_TOO_MANY_ROOMS = "User is in too many rooms!";
    public static final String SC_USER_HAS_DAILY_TICKET = "User has daily ticket!";
    public static final String SC_USER_HAS_NO_ORDER = "User has no registered order!";


    public static final String TRANSFER_FULL_ORDER = "full order transfer";
    public static final String TRANSFER_ROOM = "room transfer";
    public static final String EXCHANGE_ROOM = "room exchange";



    public static final String LANG_PRETIX = "en";
}
