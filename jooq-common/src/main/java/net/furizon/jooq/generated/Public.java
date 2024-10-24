/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated;


import java.util.Arrays;
import java.util.List;

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

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.13"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.authentications</code>.
     */
    public final Authentications AUTHENTICATIONS = Authentications.AUTHENTICATIONS;

    /**
     * The table <code>public.events</code>.
     */
    public final Events EVENTS = Events.EVENTS;

    /**
     * The table <code>public.fursuits</code>.
     */
    public final Fursuits FURSUITS = Fursuits.FURSUITS;

    /**
     * The table <code>public.fursuits_events</code>.
     */
    public final FursuitsEvents FURSUITS_EVENTS = FursuitsEvents.FURSUITS_EVENTS;

    /**
     * The table <code>public.group_permissions</code>.
     */
    public final GroupPermissions GROUP_PERMISSIONS = GroupPermissions.GROUP_PERMISSIONS;

    /**
     * The table <code>public.groups</code>.
     */
    public final Groups GROUPS = Groups.GROUPS;

    /**
     * The table <code>public.media</code>.
     */
    public final Media MEDIA = Media.MEDIA;

    /**
     * The table <code>public.membership_cards</code>.
     */
    public final MembershipCards MEMBERSHIP_CARDS = MembershipCards.MEMBERSHIP_CARDS;

    /**
     * The table <code>public.membership_info</code>.
     */
    public final MembershipInfo MEMBERSHIP_INFO = MembershipInfo.MEMBERSHIP_INFO;

    /**
     * The table <code>public.orders</code>.
     */
    public final Orders ORDERS = Orders.ORDERS;

    /**
     * The table <code>public.room_guests</code>.
     */
    public final RoomGuests ROOM_GUESTS = RoomGuests.ROOM_GUESTS;

    /**
     * The table <code>public.rooms</code>.
     */
    public final Rooms ROOMS = Rooms.ROOMS;

    /**
     * The table <code>public.schema_migrations</code>.
     */
    public final SchemaMigrations SCHEMA_MIGRATIONS = SchemaMigrations.SCHEMA_MIGRATIONS;

    /**
     * The table <code>public.user_group</code>.
     */
    public final UserGroup USER_GROUP = UserGroup.USER_GROUP;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Authentications.AUTHENTICATIONS,
            Events.EVENTS,
            Fursuits.FURSUITS,
            FursuitsEvents.FURSUITS_EVENTS,
            GroupPermissions.GROUP_PERMISSIONS,
            Groups.GROUPS,
            Media.MEDIA,
            MembershipCards.MEMBERSHIP_CARDS,
            MembershipInfo.MEMBERSHIP_INFO,
            Orders.ORDERS,
            RoomGuests.ROOM_GUESTS,
            Rooms.ROOMS,
            SchemaMigrations.SCHEMA_MIGRATIONS,
            UserGroup.USER_GROUP,
            Users.USERS
        );
    }
}
