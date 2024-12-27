/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated.tables;


import java.time.OffsetDateTime;
import java.util.Collection;

import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.Keys;
import net.furizon.jooq.generated.Public;
import net.furizon.jooq.generated.tables.ExchangeConfirmationStatus.ExchangeConfirmationStatusPath;
import net.furizon.jooq.generated.tables.Fursuits.FursuitsPath;
import net.furizon.jooq.generated.tables.FursuitsEvents.FursuitsEventsPath;
import net.furizon.jooq.generated.tables.MembershipInfo.MembershipInfoPath;
import net.furizon.jooq.generated.tables.Orders.OrdersPath;
import net.furizon.jooq.generated.tables.Users.UsersPath;

import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.JSON;
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
public class Events extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.events</code>
     */
    public static final Events EVENTS = new Events();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.events.id</code>.
     */
    public final TableField<Record, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.events.event_slug</code>.
     */
    public final TableField<Record, String> EVENT_SLUG = createField(DSL.name("event_slug"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.events.event_date_to</code>.
     */
    public final TableField<Record, OffsetDateTime> EVENT_DATE_TO = createField(DSL.name("event_date_to"), SQLDataType.TIMESTAMPWITHTIMEZONE(6), this, "");

    /**
     * The column <code>public.events.event_date_from</code>.
     */
    public final TableField<Record, OffsetDateTime> EVENT_DATE_FROM = createField(DSL.name("event_date_from"), SQLDataType.TIMESTAMPWITHTIMEZONE(6), this, "");

    /**
     * The column <code>public.events.event_is_current</code>.
     */
    public final TableField<Record, Boolean> EVENT_IS_CURRENT = createField(DSL.name("event_is_current"), SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.events.event_public_url</code>.
     */
    public final TableField<Record, String> EVENT_PUBLIC_URL = createField(DSL.name("event_public_url"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.events.event_names_json</code>.
     */
    public final TableField<Record, JSON> EVENT_NAMES_JSON = createField(DSL.name("event_names_json"), SQLDataType.JSON, this, "");

    private Events(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Events(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.events</code> table reference
     */
    public Events(String alias) {
        this(DSL.name(alias), EVENTS);
    }

    /**
     * Create an aliased <code>public.events</code> table reference
     */
    public Events(Name alias) {
        this(alias, EVENTS);
    }

    /**
     * Create a <code>public.events</code> table reference
     */
    public Events() {
        this(DSL.name("events"), null);
    }

    public <O extends Record> Events(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, EVENTS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class EventsPath extends Events implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> EventsPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private EventsPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public EventsPath as(String alias) {
            return new EventsPath(DSL.name(alias), this);
        }

        @Override
        public EventsPath as(Name alias) {
            return new EventsPath(alias, this);
        }

        @Override
        public EventsPath as(Table<?> alias) {
            return new EventsPath(alias.getQualifiedName(), this);
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
        return Keys.EVENT_PKEY;
    }

    private transient ExchangeConfirmationStatusPath _exchangeConfirmationStatus;

    /**
     * Get the implicit to-many join path to the
     * <code>public.exchange_confirmation_status</code> table
     */
    public ExchangeConfirmationStatusPath exchangeConfirmationStatus() {
        if (_exchangeConfirmationStatus == null)
            _exchangeConfirmationStatus = new ExchangeConfirmationStatusPath(this, null, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_EVENT_FK.getInverseKey());

        return _exchangeConfirmationStatus;
    }

    private transient FursuitsEventsPath _fursuitsEvents;

    /**
     * Get the implicit to-many join path to the
     * <code>public.fursuits_events</code> table
     */
    public FursuitsEventsPath fursuitsEvents() {
        if (_fursuitsEvents == null)
            _fursuitsEvents = new FursuitsEventsPath(this, null, Keys.FURSUITS_EVENTS__FURSUITS_EVENTS_EVENT_FK.getInverseKey());

        return _fursuitsEvents;
    }

    private transient MembershipInfoPath _membershipInfo;

    /**
     * Get the implicit to-many join path to the
     * <code>public.membership_info</code> table
     */
    public MembershipInfoPath membershipInfo() {
        if (_membershipInfo == null)
            _membershipInfo = new MembershipInfoPath(this, null, Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_UPDATED_EVENT_ID.getInverseKey());

        return _membershipInfo;
    }

    private transient OrdersPath _orders;

    /**
     * Get the implicit to-many join path to the <code>public.orders</code>
     * table
     */
    public OrdersPath orders() {
        if (_orders == null)
            _orders = new OrdersPath(this, null, Keys.ORDERS__ORDERS_EVENTS_ID.getInverseKey());

        return _orders;
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.users</code>
     * table, via the <code>exchange_confirmation_status_source_user_fk</code>
     * key
     */
    public UsersPath exchangeConfirmationStatusSourceUserFk() {
        return exchangeConfirmationStatus().exchangeConfirmationStatusSourceUserFk();
    }

    /**
     * Get the implicit many-to-many join path to the
     * <code>public.fursuits</code> table
     */
    public FursuitsPath fursuits() {
        return fursuitsEvents().fursuits();
    }

    /**
     * Get the implicit many-to-many join path to the <code>public.users</code>
     * table, via the <code>orders_users_id</code> key
     */
    public UsersPath ordersUsersId() {
        return orders().users();
    }

    @Override
    public Events as(String alias) {
        return new Events(DSL.name(alias), this);
    }

    @Override
    public Events as(Name alias) {
        return new Events(alias, this);
    }

    @Override
    public Events as(Table<?> alias) {
        return new Events(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Events rename(String name) {
        return new Events(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Events rename(Name name) {
        return new Events(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Events rename(Table<?> name) {
        return new Events(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events where(Condition condition) {
        return new Events(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Events where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Events where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Events where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Events where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Events whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
