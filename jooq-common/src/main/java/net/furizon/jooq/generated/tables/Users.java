/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated.tables;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.Keys;
import net.furizon.jooq.generated.Public;
import net.furizon.jooq.generated.tables.Authentications.AuthenticationsPath;
import net.furizon.jooq.generated.tables.Events.EventsPath;
import net.furizon.jooq.generated.tables.ExchangeConfirmationStatus.ExchangeConfirmationStatusPath;
import net.furizon.jooq.generated.tables.Fursuits.FursuitsPath;
import net.furizon.jooq.generated.tables.Media.MediaPath;
import net.furizon.jooq.generated.tables.MembershipCards.MembershipCardsPath;
import net.furizon.jooq.generated.tables.MembershipInfo.MembershipInfoPath;
import net.furizon.jooq.generated.tables.Orders.OrdersPath;
import net.furizon.jooq.generated.tables.Roles.RolesPath;
import net.furizon.jooq.generated.tables.RoomGuests.RoomGuestsPath;
import net.furizon.jooq.generated.tables.UserHasRole.UserHasRolePath;

import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


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
public class Users extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.users</code>
     */
    public static final Users USERS = new Users();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.users.user_id</code>.
     */
    public final TableField<Record, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.users.user_fursona_name</code>.
     */
    public final TableField<Record, String> USER_FURSONA_NAME = createField(DSL.name("user_fursona_name"), SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>public.users.user_locale</code>.
     */
    public final TableField<Record, String> USER_LOCALE = createField(DSL.name("user_locale"), SQLDataType.VARCHAR(8).defaultValue(DSL.field(DSL.raw("'it'::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.users.media_id_propic</code>.
     */
    public final TableField<Record, Long> MEDIA_ID_PROPIC = createField(DSL.name("media_id_propic"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.users.show_in_nosecount</code>.
     */
    public final TableField<Record, Boolean> SHOW_IN_NOSECOUNT = createField(DSL.name("show_in_nosecount"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("true"), SQLDataType.BOOLEAN)), this, "");

    private Users(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Users(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(String alias) {
        this(DSL.name(alias), USERS);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(Name alias) {
        this(alias, USERS);
    }

    /**
     * Create a <code>public.users</code> table reference
     */
    public Users() {
        this(DSL.name("users"), null);
    }

    public <O extends Record> Users(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, USERS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UsersPath extends Users implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> UsersPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private UsersPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public UsersPath as(String alias) {
            return new UsersPath(DSL.name(alias), this);
        }

        @Override
        public UsersPath as(Name alias) {
            return new UsersPath(alias, this);
        }

        @Override
        public UsersPath as(Table<?> alias) {
            return new UsersPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<Record, Long> getIdentity() {
        return (Identity<Record, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return Keys.USERS_PKEY;
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.USERS__USER_MEDIA_FK);
    }

    private transient MediaPath _media;

    /**
     * Get the implicit join path to the <code>public.media</code> table.
     */
    public MediaPath media() {
        if (_media == null)
            _media = new MediaPath(this, Keys.USERS__USER_MEDIA_FK, null);

        return _media;
    }

    private transient AuthenticationsPath _authentications;

    /**
     * Get the implicit to-many join path to the
     * <code>public.authentications</code> table
     */
    public AuthenticationsPath authentications() {
        if (_authentications == null)
            _authentications = new AuthenticationsPath(this, null, Keys.AUTHENTICATIONS__AUTHENTICATIONS_USERS_FK.getInverseKey());

        return _authentications;
    }

    private transient MembershipCardsPath _membershipCards;

    /**
     * Get the implicit to-many join path to the
     * <code>public.membership_cards</code> table
     */
    public MembershipCardsPath membershipCards() {
        if (_membershipCards == null)
            _membershipCards = new MembershipCardsPath(this, null, Keys.MEMBERSHIP_CARDS__CARD_USER_FK.getInverseKey());

        return _membershipCards;
    }

    private transient ExchangeConfirmationStatusPath _exchangeConfirmationStatusSourceUserFk;

    /**
     * Get the implicit to-many join path to the
     * <code>public.exchange_confirmation_status</code> table, via the
     * <code>exchange_confirmation_status_source_user_fk</code> key
     */
    public ExchangeConfirmationStatusPath exchangeConfirmationStatusSourceUserFk() {
        if (_exchangeConfirmationStatusSourceUserFk == null)
            _exchangeConfirmationStatusSourceUserFk = new ExchangeConfirmationStatusPath(this, null, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_SOURCE_USER_FK.getInverseKey());

        return _exchangeConfirmationStatusSourceUserFk;
    }

    private transient ExchangeConfirmationStatusPath _exchangeConfirmationStatusTargetUserFk;

    /**
     * Get the implicit to-many join path to the
     * <code>public.exchange_confirmation_status</code> table, via the
     * <code>exchange_confirmation_status_target_user_fk</code> key
     */
    public ExchangeConfirmationStatusPath exchangeConfirmationStatusTargetUserFk() {
        if (_exchangeConfirmationStatusTargetUserFk == null)
            _exchangeConfirmationStatusTargetUserFk = new ExchangeConfirmationStatusPath(this, null, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_TARGET_USER_FK.getInverseKey());

        return _exchangeConfirmationStatusTargetUserFk;
    }

    private transient FursuitsPath _fursuits;

    /**
     * Get the implicit to-many join path to the <code>public.fursuits</code>
     * table
     */
    public FursuitsPath fursuits() {
        if (_fursuits == null)
            _fursuits = new FursuitsPath(this, null, Keys.FURSUITS__FURSUITS_USERS_FK.getInverseKey());

        return _fursuits;
    }

    private transient MembershipInfoPath _membershipInfo;

    /**
     * Get the implicit to-many join path to the
     * <code>public.membership_info</code> table
     */
    public MembershipInfoPath membershipInfo() {
        if (_membershipInfo == null)
            _membershipInfo = new MembershipInfoPath(this, null, Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_USERS_FK.getInverseKey());

        return _membershipInfo;
    }

    private transient OrdersPath _orders;

    /**
     * Get the implicit to-many join path to the <code>public.orders</code>
     * table
     */
    public OrdersPath orders() {
        if (_orders == null)
            _orders = new OrdersPath(this, null, Keys.ORDERS__ORDERS_USERS_ID.getInverseKey());

        return _orders;
    }

    private transient RoomGuestsPath _roomGuests;

    /**
     * Get the implicit to-many join path to the <code>public.room_guests</code>
     * table
     */
    public RoomGuestsPath roomGuests() {
        if (_roomGuests == null)
            _roomGuests = new RoomGuestsPath(this, null, Keys.ROOM_GUESTS__ROOM_GUESTS_USERS_FK.getInverseKey());

        return _roomGuests;
    }

    private transient UserHasRolePath _userHasRole;

    /**
     * Get the implicit to-many join path to the
     * <code>public.user_has_role</code> table
     */
    public UserHasRolePath userHasRole() {
        if (_userHasRole == null)
            _userHasRole = new UserHasRolePath(this, null, Keys.USER_HAS_ROLE__USER_HAS_ROLE_USER_FK.getInverseKey());

        return _userHasRole;
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.roles</code>
     * table
     */
    public RolesPath roles() {
        return userHasRole().roles();
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.events</code>
     * table, via the <code>exchange_confirmation_status_event_fk</code> key
     */
    public EventsPath exchangeConfirmationStatusEventFk() {
        return exchangeConfirmationStatusSourceUserFk().events();
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.events</code>
     * table, via the <code>orders_events_id</code> key
     */
    public EventsPath ordersEventsId() {
        return orders().events();
    }

    @Override
    public Users as(String alias) {
        return new Users(DSL.name(alias), this);
    }

    @Override
    public Users as(Name alias) {
        return new Users(alias, this);
    }

    @Override
    public Users as(Table<?> alias) {
        return new Users(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(String name) {
        return new Users(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(Name name) {
        return new Users(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(Table<?> name) {
        return new Users(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Condition condition) {
        return new Users(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
