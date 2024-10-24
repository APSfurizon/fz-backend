/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated;


import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.tables.Authentications;
import net.furizon.jooq.generated.tables.Events;
import net.furizon.jooq.generated.tables.Fursuits;
import net.furizon.jooq.generated.tables.FursuitsEvents;
import net.furizon.jooq.generated.tables.GroupPermissions;
import net.furizon.jooq.generated.tables.Groups;
import net.furizon.jooq.generated.tables.Media;
import net.furizon.jooq.generated.tables.MembershipCards;
import net.furizon.jooq.generated.tables.MembershipInfo;
import net.furizon.jooq.generated.tables.Orders;
import net.furizon.jooq.generated.tables.RoomGuests;
import net.furizon.jooq.generated.tables.Rooms;
import net.furizon.jooq.generated.tables.SchemaMigrations;
import net.furizon.jooq.generated.tables.UserGroup;
import net.furizon.jooq.generated.tables.Users;


/**
 * Convenience access to all tables in public.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.13"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Tables {

    /**
     * The table <code>public.authentications</code>.
     */
    public static final Authentications AUTHENTICATIONS = Authentications.AUTHENTICATIONS;

    /**
     * The table <code>public.events</code>.
     */
    public static final Events EVENTS = Events.EVENTS;

    /**
     * The table <code>public.fursuits</code>.
     */
    public static final Fursuits FURSUITS = Fursuits.FURSUITS;

    /**
     * The table <code>public.fursuits_events</code>.
     */
    public static final FursuitsEvents FURSUITS_EVENTS = FursuitsEvents.FURSUITS_EVENTS;

    /**
     * The table <code>public.group_permissions</code>.
     */
    public static final GroupPermissions GROUP_PERMISSIONS = GroupPermissions.GROUP_PERMISSIONS;

    /**
     * The table <code>public.groups</code>.
     */
    public static final Groups GROUPS = Groups.GROUPS;

    /**
     * The table <code>public.media</code>.
     */
    public static final Media MEDIA = Media.MEDIA;

    /**
     * The table <code>public.membership_cards</code>.
     */
    public static final MembershipCards MEMBERSHIP_CARDS = MembershipCards.MEMBERSHIP_CARDS;

    /**
     * The table <code>public.membership_info</code>.
     */
    public static final MembershipInfo MEMBERSHIP_INFO = MembershipInfo.MEMBERSHIP_INFO;

    /**
     * The table <code>public.orders</code>.
     */
    public static final Orders ORDERS = Orders.ORDERS;

    /**
     * The table <code>public.room_guests</code>.
     */
    public static final RoomGuests ROOM_GUESTS = RoomGuests.ROOM_GUESTS;

    /**
     * The table <code>public.rooms</code>.
     */
    public static final Rooms ROOMS = Rooms.ROOMS;

    /**
     * The table <code>public.schema_migrations</code>.
     */
    public static final SchemaMigrations SCHEMA_MIGRATIONS = SchemaMigrations.SCHEMA_MIGRATIONS;

    /**
     * The table <code>public.user_group</code>.
     */
    public static final UserGroup USER_GROUP = UserGroup.USER_GROUP;

    /**
     * The table <code>public.users</code>.
     */
    public static final Users USERS = Users.USERS;
}
