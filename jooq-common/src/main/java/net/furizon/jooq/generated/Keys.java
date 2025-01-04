/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated;


import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.tables.Authentications;
import net.furizon.jooq.generated.tables.Events;
import net.furizon.jooq.generated.tables.Fursuits;
import net.furizon.jooq.generated.tables.FursuitsEvents;
import net.furizon.jooq.generated.tables.Media;
import net.furizon.jooq.generated.tables.MembershipCards;
import net.furizon.jooq.generated.tables.MembershipInfo;
import net.furizon.jooq.generated.tables.Orders;
import net.furizon.jooq.generated.tables.Permission;
import net.furizon.jooq.generated.tables.Roles;
import net.furizon.jooq.generated.tables.RoomGuests;
import net.furizon.jooq.generated.tables.Rooms;
import net.furizon.jooq.generated.tables.SchemaMigrations;
import net.furizon.jooq.generated.tables.Sessions;
import net.furizon.jooq.generated.tables.UserHasRole;
import net.furizon.jooq.generated.tables.Users;

import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.13"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<Record> AUTHENTICATIONS_PKEY = Internal.createUniqueKey(Authentications.AUTHENTICATIONS, DSL.name("authentications_pkey"), new TableField[] { Authentications.AUTHENTICATIONS.AUTHENTICATION_ID }, true);
    public static final UniqueKey<Record> AUTHENTICATIONS_UNIQUE_USER_ID = Internal.createUniqueKey(Authentications.AUTHENTICATIONS, DSL.name("authentications_unique_user_id"), new TableField[] { Authentications.AUTHENTICATIONS.USER_ID }, true);
    public static final UniqueKey<Record> EVENT_PKEY = Internal.createUniqueKey(Events.EVENTS, DSL.name("event_pkey"), new TableField[] { Events.EVENTS.ID }, true);
    public static final UniqueKey<Record> FURSUITS_PKEY = Internal.createUniqueKey(Fursuits.FURSUITS, DSL.name("fursuits_pkey"), new TableField[] { Fursuits.FURSUITS.FURSUIT_ID }, true);
    public static final UniqueKey<Record> FURSUITS_EVENTS_PK = Internal.createUniqueKey(FursuitsEvents.FURSUITS_EVENTS, DSL.name("fursuits_events_pk"), new TableField[] { FursuitsEvents.FURSUITS_EVENTS.EVENT_ID, FursuitsEvents.FURSUITS_EVENTS.FURSUIT_ID }, true);
    public static final UniqueKey<Record> MEDIA_PKEY = Internal.createUniqueKey(Media.MEDIA, DSL.name("media_pkey"), new TableField[] { Media.MEDIA.MEDIA_ID }, true);
    public static final UniqueKey<Record> CARDS_PKEY = Internal.createUniqueKey(MembershipCards.MEMBERSHIP_CARDS, DSL.name("cards_pkey"), new TableField[] { MembershipCards.MEMBERSHIP_CARDS.CARD_DB_ID }, true);
    public static final UniqueKey<Record> MEMBERSHIP_INFO_ID_PKEY = Internal.createUniqueKey(MembershipInfo.MEMBERSHIP_INFO, DSL.name("membership_info_id_pkey"), new TableField[] { MembershipInfo.MEMBERSHIP_INFO.ID }, true);
    public static final UniqueKey<Record> ORDERS_PKEY = Internal.createUniqueKey(Orders.ORDERS, DSL.name("orders_pkey"), new TableField[] { Orders.ORDERS.ID }, true);
    public static final UniqueKey<Record> PERMISSION_PK = Internal.createUniqueKey(Permission.PERMISSION, DSL.name("permission_pk"), new TableField[] { Permission.PERMISSION.ROLE_ID, Permission.PERMISSION.PERMISSION_VALUE }, true);
    public static final UniqueKey<Record> ROLES_PK = Internal.createUniqueKey(Roles.ROLES, DSL.name("roles_pk"), new TableField[] { Roles.ROLES.ROLE_ID }, true);
    public static final UniqueKey<Record> ROLES_UNIQUE_INTERNAL_NAME = Internal.createUniqueKey(Roles.ROLES, DSL.name("roles_unique_internal_name"), new TableField[] { Roles.ROLES.INTERNAL_NAME }, true);
    public static final UniqueKey<Record> ROOM_GUESTS_PKEY = Internal.createUniqueKey(RoomGuests.ROOM_GUESTS, DSL.name("room_guests_pkey"), new TableField[] { RoomGuests.ROOM_GUESTS.ROOM_GUEST_ID }, true);
    public static final UniqueKey<Record> ROOMS_PKEY = Internal.createUniqueKey(Rooms.ROOMS, DSL.name("rooms_pkey"), new TableField[] { Rooms.ROOMS.ROOM_ID }, true);
    public static final UniqueKey<Record> SCHEMA_MIGRATIONS_PKEY = Internal.createUniqueKey(SchemaMigrations.SCHEMA_MIGRATIONS, DSL.name("schema_migrations_pkey"), new TableField[] { SchemaMigrations.SCHEMA_MIGRATIONS.VERSION }, true);
    public static final UniqueKey<Record> SESSIONS_PKEY = Internal.createUniqueKey(Sessions.SESSIONS, DSL.name("sessions_pkey"), new TableField[] { Sessions.SESSIONS.ID }, true);
    public static final UniqueKey<Record> USER_HAS_ROLE_PK = Internal.createUniqueKey(UserHasRole.USER_HAS_ROLE, DSL.name("user_has_role_pk"), new TableField[] { UserHasRole.USER_HAS_ROLE.USER_ID, UserHasRole.USER_HAS_ROLE.ROLE_ID }, true);
    public static final UniqueKey<Record> USERS_PKEY = Internal.createUniqueKey(Users.USERS, DSL.name("users_pkey"), new TableField[] { Users.USERS.USER_ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<Record, Record> AUTHENTICATIONS__AUTHENTICATIONS_USERS_FK = Internal.createForeignKey(Authentications.AUTHENTICATIONS, DSL.name("authentications_users_fk"), new TableField[] { Authentications.AUTHENTICATIONS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS__FURSUITS_MEDIA_FK = Internal.createForeignKey(Fursuits.FURSUITS, DSL.name("fursuits_media_fk"), new TableField[] { Fursuits.FURSUITS.MEDIA_ID_PROPIC }, Keys.MEDIA_PKEY, new TableField[] { Media.MEDIA.MEDIA_ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS__FURSUITS_USERS_FK = Internal.createForeignKey(Fursuits.FURSUITS, DSL.name("fursuits_users_fk"), new TableField[] { Fursuits.FURSUITS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS_EVENTS__FURSUITS_EVENTS_EVENT_FK = Internal.createForeignKey(FursuitsEvents.FURSUITS_EVENTS, DSL.name("fursuits_events_event_fk"), new TableField[] { FursuitsEvents.FURSUITS_EVENTS.EVENT_ID }, Keys.EVENT_PKEY, new TableField[] { Events.EVENTS.ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS_EVENTS__FURSUITS_EVENTS_FURSUIT_FK = Internal.createForeignKey(FursuitsEvents.FURSUITS_EVENTS, DSL.name("fursuits_events_fursuit_fk"), new TableField[] { FursuitsEvents.FURSUITS_EVENTS.FURSUIT_ID }, Keys.FURSUITS_PKEY, new TableField[] { Fursuits.FURSUITS.FURSUIT_ID }, true);
    public static final ForeignKey<Record, Record> MEMBERSHIP_CARDS__CARD_USER_FK = Internal.createForeignKey(MembershipCards.MEMBERSHIP_CARDS, DSL.name("card_user_fk"), new TableField[] { MembershipCards.MEMBERSHIP_CARDS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> MEMBERSHIP_CARDS__MEMBERSHIP_CARDS_ORDER_FK = Internal.createForeignKey(MembershipCards.MEMBERSHIP_CARDS, DSL.name("membership_cards_order_fk"), new TableField[] { MembershipCards.MEMBERSHIP_CARDS.CREATED_FOR_ORDER }, Keys.ORDERS_PKEY, new TableField[] { Orders.ORDERS.ID }, true);
    public static final ForeignKey<Record, Record> MEMBERSHIP_INFO__MEMBERSHIP_INFO_UPDATED_EVENT_ID = Internal.createForeignKey(MembershipInfo.MEMBERSHIP_INFO, DSL.name("membership_info_updated_event_id"), new TableField[] { MembershipInfo.MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID }, Keys.EVENT_PKEY, new TableField[] { Events.EVENTS.ID }, true);
    public static final ForeignKey<Record, Record> MEMBERSHIP_INFO__MEMBERSHIP_INFO_USERS_FK = Internal.createForeignKey(MembershipInfo.MEMBERSHIP_INFO, DSL.name("membership_info_users_fk"), new TableField[] { MembershipInfo.MEMBERSHIP_INFO.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> ORDERS__ORDERS_EVENTS_ID = Internal.createForeignKey(Orders.ORDERS, DSL.name("orders_events_id"), new TableField[] { Orders.ORDERS.EVENT_ID }, Keys.EVENT_PKEY, new TableField[] { Events.EVENTS.ID }, true);
    public static final ForeignKey<Record, Record> ORDERS__ORDERS_USERS_ID = Internal.createForeignKey(Orders.ORDERS, DSL.name("orders_users_id"), new TableField[] { Orders.ORDERS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> PERMISSION__PERMISSION_ROLE_FK = Internal.createForeignKey(Permission.PERMISSION, DSL.name("permission_role_fk"), new TableField[] { Permission.PERMISSION.ROLE_ID }, Keys.ROLES_PK, new TableField[] { Roles.ROLES.ROLE_ID }, true);
    public static final ForeignKey<Record, Record> ROOM_GUESTS__ROOM_GUESTS_ROOMS_FK = Internal.createForeignKey(RoomGuests.ROOM_GUESTS, DSL.name("room_guests_rooms_fk"), new TableField[] { RoomGuests.ROOM_GUESTS.ROOM_ID }, Keys.ROOMS_PKEY, new TableField[] { Rooms.ROOMS.ROOM_ID }, true);
    public static final ForeignKey<Record, Record> ROOM_GUESTS__ROOM_GUESTS_USERS_FK = Internal.createForeignKey(RoomGuests.ROOM_GUESTS, DSL.name("room_guests_users_fk"), new TableField[] { RoomGuests.ROOM_GUESTS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> ROOMS__ROOMS_ORDERS_ID = Internal.createForeignKey(Rooms.ROOMS, DSL.name("rooms_orders_id"), new TableField[] { Rooms.ROOMS.ORDER_ID }, Keys.ORDERS_PKEY, new TableField[] { Orders.ORDERS.ID }, true);
    public static final ForeignKey<Record, Record> USER_HAS_ROLE__USER_HAS_ROLE_EVENT_FK = Internal.createForeignKey(UserHasRole.USER_HAS_ROLE, DSL.name("user_has_role_event_fk"), new TableField[] { UserHasRole.USER_HAS_ROLE.TEMP_EVENT_ID }, Keys.EVENT_PKEY, new TableField[] { Events.EVENTS.ID }, true);
    public static final ForeignKey<Record, Record> USER_HAS_ROLE__USER_HAS_ROLE_ROLE_FK = Internal.createForeignKey(UserHasRole.USER_HAS_ROLE, DSL.name("user_has_role_role_fk"), new TableField[] { UserHasRole.USER_HAS_ROLE.ROLE_ID }, Keys.ROLES_PK, new TableField[] { Roles.ROLES.ROLE_ID }, true);
    public static final ForeignKey<Record, Record> USER_HAS_ROLE__USER_HAS_ROLE_USER_FK = Internal.createForeignKey(UserHasRole.USER_HAS_ROLE, DSL.name("user_has_role_user_fk"), new TableField[] { UserHasRole.USER_HAS_ROLE.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> USERS__USER_MEDIA_FK = Internal.createForeignKey(Users.USERS, DSL.name("user_media_fk"), new TableField[] { Users.USERS.MEDIA_ID_PROPIC }, Keys.MEDIA_PKEY, new TableField[] { Media.MEDIA.MEDIA_ID }, true);
}
