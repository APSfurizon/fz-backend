/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.Keys;
import net.furizon.jooq.generated.Public;
import net.furizon.jooq.generated.tables.Events.EventsPath;
import net.furizon.jooq.generated.tables.Rooms.RoomsPath;
import net.furizon.jooq.generated.tables.Users.UsersPath;

import org.jetbrains.annotations.Nullable;
import org.jooq.Check;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
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
import org.jooq.impl.Internal;
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
public class Orders extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.orders</code>
     */
    public static final Orders ORDERS = new Orders();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.orders.id</code>.
     */
    public final TableField<Record, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.orders.order_code</code>.
     */
    public final TableField<Record, String> ORDER_CODE = createField(DSL.name("order_code"), SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>public.orders.order_answers_json</code>.
     */
    public final TableField<Record, JSON> ORDER_ANSWERS_JSON = createField(DSL.name("order_answers_json"), SQLDataType.JSON, this, "");

    /**
     * The column <code>public.orders.order_status</code>.
     */
    public final TableField<Record, Short> ORDER_STATUS = createField(DSL.name("order_status"), SQLDataType.SMALLINT.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.SMALLINT)), this, "");

    /**
     * The column <code>public.orders.order_answers_main_position_id</code>.
     */
    public final TableField<Record, Integer> ORDER_ANSWERS_MAIN_POSITION_ID = createField(DSL.name("order_answers_main_position_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.orders.order_daily_days</code>.
     */
    public final TableField<Record, Long> ORDER_DAILY_DAYS = createField(DSL.name("order_daily_days"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.orders.order_extra_days_type</code>.
     */
    public final TableField<Record, Short> ORDER_EXTRA_DAYS_TYPE = createField(DSL.name("order_extra_days_type"), SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.orders.order_room_capacity</code>.
     */
    public final TableField<Record, Short> ORDER_ROOM_CAPACITY = createField(DSL.name("order_room_capacity"), SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.orders.order_hotel_location</code>.
     */
    public final TableField<Record, String> ORDER_HOTEL_LOCATION = createField(DSL.name("order_hotel_location"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.orders.has_membership</code>.
     */
    public final TableField<Record, Boolean> HAS_MEMBERSHIP = createField(DSL.name("has_membership"), SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.orders.order_secret</code>.
     */
    public final TableField<Record, String> ORDER_SECRET = createField(DSL.name("order_secret"), SQLDataType.VARCHAR(32), this, "");

    /**
     * The column <code>public.orders.order_sponsorship_type</code>.
     */
    public final TableField<Record, Short> ORDER_SPONSORSHIP_TYPE = createField(DSL.name("order_sponsorship_type"), SQLDataType.SMALLINT.nullable(false), this, "");

    /**
     * The column <code>public.orders.event_id</code>.
     */
    public final TableField<Record, Long> EVENT_ID = createField(DSL.name("event_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.orders.user_id</code>.
     */
    public final TableField<Record, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.orders.creation_ts</code>.
     */
    public final TableField<Record, LocalDateTime> CREATION_TS = createField(DSL.name("creation_ts"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private Orders(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Orders(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.orders</code> table reference
     */
    public Orders(String alias) {
        this(DSL.name(alias), ORDERS);
    }

    /**
     * Create an aliased <code>public.orders</code> table reference
     */
    public Orders(Name alias) {
        this(alias, ORDERS);
    }

    /**
     * Create a <code>public.orders</code> table reference
     */
    public Orders() {
        this(DSL.name("orders"), null);
    }

    public <O extends Record> Orders(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, ORDERS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class OrdersPath extends Orders implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> OrdersPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private OrdersPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public OrdersPath as(String alias) {
            return new OrdersPath(DSL.name(alias), this);
        }

        @Override
        public OrdersPath as(Name alias) {
            return new OrdersPath(alias, this);
        }

        @Override
        public OrdersPath as(Table<?> alias) {
            return new OrdersPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return Keys.ORDERS_PKEY;
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.ORDERS__ORDERS_EVENTS_ID, Keys.ORDERS__ORDERS_USERS_ID);
    }

    private transient EventsPath _events;

    /**
     * Get the implicit join path to the <code>public.events</code> table.
     */
    public EventsPath events() {
        if (_events == null)
            _events = new EventsPath(this, Keys.ORDERS__ORDERS_EVENTS_ID, null);

        return _events;
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.ORDERS__ORDERS_USERS_ID, null);

        return _users;
    }

    private transient RoomsPath _rooms;

    /**
     * Get the implicit to-many join path to the <code>public.rooms</code> table
     */
    public RoomsPath rooms() {
        if (_rooms == null)
            _rooms = new RoomsPath(this, null, Keys.ROOMS__ROOMS_ORDERS_ID.getInverseKey());

        return _rooms;
    }

    @Override
    public List<Check<Record>> getChecks() {
        return Arrays.asList(
            Internal.createCheck(this, DSL.name("orders_extra_days_check"), "(((order_extra_days_type >= 0) AND (order_extra_days_type <= 3)))", true),
            Internal.createCheck(this, DSL.name("orders_sponsorship_check"), "(((order_sponsorship_type >= 0) AND (order_sponsorship_type <= 2)))", true),
            Internal.createCheck(this, DSL.name("orders_status_check"), "(((order_status >= 0) AND (order_status <= 3)))", true)
        );
    }

    @Override
    public Orders as(String alias) {
        return new Orders(DSL.name(alias), this);
    }

    @Override
    public Orders as(Name alias) {
        return new Orders(alias, this);
    }

    @Override
    public Orders as(Table<?> alias) {
        return new Orders(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Orders rename(String name) {
        return new Orders(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Orders rename(Name name) {
        return new Orders(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Orders rename(Table<?> name) {
        return new Orders(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders where(Condition condition) {
        return new Orders(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Orders where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Orders where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Orders where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Orders where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Orders whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
