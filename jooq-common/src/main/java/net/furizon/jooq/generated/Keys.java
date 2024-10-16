/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated;


import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.tables.Authentications;
import net.furizon.jooq.generated.tables.Events;
import net.furizon.jooq.generated.tables.Fursuits;
import net.furizon.jooq.generated.tables.Groups;
import net.furizon.jooq.generated.tables.Media;
import net.furizon.jooq.generated.tables.MediaTags;
import net.furizon.jooq.generated.tables.Orders;
import net.furizon.jooq.generated.tables.RoomGuests;
import net.furizon.jooq.generated.tables.Rooms;
import net.furizon.jooq.generated.tables.Tags;
import net.furizon.jooq.generated.tables.UserGroup;
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
    public static final UniqueKey<Record> UK558QV80E1CP0TIYV7YYBTGQQ2 = Internal.createUniqueKey(Authentications.AUTHENTICATIONS, DSL.name("uk558qv80e1cp0tiyv7yybtgqq2"), new TableField[] { Authentications.AUTHENTICATIONS.AUTHENTICATION_EMAIL }, true);
    public static final UniqueKey<Record> UKSJIUBBLOQ36P1Q26IHVY31IG1 = Internal.createUniqueKey(Authentications.AUTHENTICATIONS, DSL.name("uksjiubbloq36p1q26ihvy31ig1"), new TableField[] { Authentications.AUTHENTICATIONS.USER_ID }, true);
    public static final UniqueKey<Record> EVENTS_PKEY = Internal.createUniqueKey(Events.EVENTS, DSL.name("events_pkey"), new TableField[] { Events.EVENTS.EVENT_SLUG }, true);
    public static final UniqueKey<Record> FURSUITS_PKEY = Internal.createUniqueKey(Fursuits.FURSUITS, DSL.name("fursuits_pkey"), new TableField[] { Fursuits.FURSUITS.FURSUIT_ID }, true);
    public static final UniqueKey<Record> UK47Y138Q5E9DQWRWTJWI5OVLMW = Internal.createUniqueKey(Fursuits.FURSUITS, DSL.name("uk47y138q5e9dqwrwtjwi5ovlmw"), new TableField[] { Fursuits.FURSUITS.MEDIA_ID }, true);
    public static final UniqueKey<Record> GROUPS_PKEY = Internal.createUniqueKey(Groups.GROUPS, DSL.name("groups_pkey"), new TableField[] { Groups.GROUPS.GROUP_ID }, true);
    public static final UniqueKey<Record> MEDIA_PKEY = Internal.createUniqueKey(Media.MEDIA, DSL.name("media_pkey"), new TableField[] { Media.MEDIA.MEDIA_ID }, true);
    public static final UniqueKey<Record> MEDIA_TAGS_PKEY = Internal.createUniqueKey(MediaTags.MEDIA_TAGS, DSL.name("media_tags_pkey"), new TableField[] { MediaTags.MEDIA_TAGS.MEDIA_TAG_ID }, true);
    public static final UniqueKey<Record> ORDERS_PKEY = Internal.createUniqueKey(Orders.ORDERS, DSL.name("orders_pkey"), new TableField[] { Orders.ORDERS.CODE }, true);
    public static final UniqueKey<Record> ROOM_GUESTS_PKEY = Internal.createUniqueKey(RoomGuests.ROOM_GUESTS, DSL.name("room_guests_pkey"), new TableField[] { RoomGuests.ROOM_GUESTS.ROOM_GUEST_ID }, true);
    public static final UniqueKey<Record> ROOMS_PKEY = Internal.createUniqueKey(Rooms.ROOMS, DSL.name("rooms_pkey"), new TableField[] { Rooms.ROOMS.ROOM_ID }, true);
    public static final UniqueKey<Record> UK3VXYR1CTRNQ6O6K36MF8KAD20 = Internal.createUniqueKey(Rooms.ROOMS, DSL.name("uk3vxyr1ctrnq6o6k36mf8kad20"), new TableField[] { Rooms.ROOMS.ORDER_ID }, true);
    public static final UniqueKey<Record> TAGS_PKEY = Internal.createUniqueKey(Tags.TAGS, DSL.name("tags_pkey"), new TableField[] { Tags.TAGS.TAG_ID }, true);
    public static final UniqueKey<Record> UKRLLOBBOR6YGDSIT4DSVACBD90 = Internal.createUniqueKey(Tags.TAGS, DSL.name("ukrllobbor6ygdsit4dsvacbd90"), new TableField[] { Tags.TAGS.TAG_CODE }, true);
    public static final UniqueKey<Record> USER_GROUP_PKEY = Internal.createUniqueKey(UserGroup.USER_GROUP, DSL.name("user_group_pkey"), new TableField[] { UserGroup.USER_GROUP.USER_GROUP_ID }, true);
    public static final UniqueKey<Record> USERS_PKEY = Internal.createUniqueKey(Users.USERS, DSL.name("users_pkey"), new TableField[] { Users.USERS.USER_ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<Record, Record> AUTHENTICATIONS__FK3NIFXK39Y3BOH8Q91NFNPFJF1 = Internal.createForeignKey(Authentications.AUTHENTICATIONS, DSL.name("fk3nifxk39y3boh8q91nfnpfjf1"), new TableField[] { Authentications.AUTHENTICATIONS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS__FKGS95COIQ3BDFGO4RRI2XXA01E = Internal.createForeignKey(Fursuits.FURSUITS, DSL.name("fkgs95coiq3bdfgo4rri2xxa01e"), new TableField[] { Fursuits.FURSUITS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> FURSUITS__FKOM4XAJCQUVF8OUNSRELSW3A2O = Internal.createForeignKey(Fursuits.FURSUITS, DSL.name("fkom4xajcquvf8ounsrelsw3a2o"), new TableField[] { Fursuits.FURSUITS.MEDIA_ID }, Keys.MEDIA_PKEY, new TableField[] { Media.MEDIA.MEDIA_ID }, true);
    public static final ForeignKey<Record, Record> MEDIA__FKND8HH0YN7QVV4PQYK8MG7L1OX = Internal.createForeignKey(Media.MEDIA, DSL.name("fknd8hh0yn7qvv4pqyk8mg7l1ox"), new TableField[] { Media.MEDIA.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> MEDIA_TAGS__FK88C8WCOPLIS4O99Y53CCN21GT = Internal.createForeignKey(MediaTags.MEDIA_TAGS, DSL.name("fk88c8wcoplis4o99y53ccn21gt"), new TableField[] { MediaTags.MEDIA_TAGS.MEDIA_ID }, Keys.MEDIA_PKEY, new TableField[] { Media.MEDIA.MEDIA_ID }, true);
    public static final ForeignKey<Record, Record> MEDIA_TAGS__FKFGINODY5NX4QLTQEY6C6S16LT = Internal.createForeignKey(MediaTags.MEDIA_TAGS, DSL.name("fkfginody5nx4qltqey6c6s16lt"), new TableField[] { MediaTags.MEDIA_TAGS.TAG_ID }, Keys.TAGS_PKEY, new TableField[] { Tags.TAGS.TAG_ID }, true);
    public static final ForeignKey<Record, Record> ORDERS__FK32QL8UBNTJ5UH44PH9659TIIH = Internal.createForeignKey(Orders.ORDERS, DSL.name("fk32ql8ubntj5uh44ph9659tiih"), new TableField[] { Orders.ORDERS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> ORDERS__FK43G2YROY6L7LFOMW37WAJKQRN = Internal.createForeignKey(Orders.ORDERS, DSL.name("fk43g2yroy6l7lfomw37wajkqrn"), new TableField[] { Orders.ORDERS.EVENT_ID }, Keys.EVENTS_PKEY, new TableField[] { Events.EVENTS.EVENT_SLUG }, true);
    public static final ForeignKey<Record, Record> ROOM_GUESTS__FKIJFN5MTG319VQTL9JPUA6XWFT = Internal.createForeignKey(RoomGuests.ROOM_GUESTS, DSL.name("fkijfn5mtg319vqtl9jpua6xwft"), new TableField[] { RoomGuests.ROOM_GUESTS.ROOM_ID }, Keys.ROOMS_PKEY, new TableField[] { Rooms.ROOMS.ROOM_ID }, true);
    public static final ForeignKey<Record, Record> ROOM_GUESTS__FKL1DL60HSAFJ4YVCHREURMFFFN = Internal.createForeignKey(RoomGuests.ROOM_GUESTS, DSL.name("fkl1dl60hsafj4yvchreurmfffn"), new TableField[] { RoomGuests.ROOM_GUESTS.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> ROOMS__FK8WMI1L6M4HI0FKSPAQDB26RIS = Internal.createForeignKey(Rooms.ROOMS, DSL.name("fk8wmi1l6m4hi0fkspaqdb26ris"), new TableField[] { Rooms.ROOMS.ORDER_ID }, Keys.ORDERS_PKEY, new TableField[] { Orders.ORDERS.CODE }, true);
    public static final ForeignKey<Record, Record> USER_GROUP__FK7K9ADE3LQBO483U9VURYXMM34 = Internal.createForeignKey(UserGroup.USER_GROUP, DSL.name("fk7k9ade3lqbo483u9vuryxmm34"), new TableField[] { UserGroup.USER_GROUP.USER_ID }, Keys.USERS_PKEY, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<Record, Record> USER_GROUP__FKBEGTGNL3OQ004958PISKO4FU4 = Internal.createForeignKey(UserGroup.USER_GROUP, DSL.name("fkbegtgnl3oq004958pisko4fu4"), new TableField[] { UserGroup.USER_GROUP.GROUP_ID }, Keys.GROUPS_PKEY, new TableField[] { Groups.GROUPS.GROUP_ID }, true);
}
