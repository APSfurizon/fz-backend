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
import net.furizon.jooq.generated.tables.Events.EventsPath;
import net.furizon.jooq.generated.tables.Users.UsersPath;

import org.jetbrains.annotations.Nullable;
import org.jooq.Check;
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
public class ExchangeConfirmationStatus extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>public.exchange_confirmation_status</code>
     */
    public static final ExchangeConfirmationStatus EXCHANGE_CONFIRMATION_STATUS = new ExchangeConfirmationStatus();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.exchange_confirmation_status.exchange_id</code>.
     */
    public final TableField<Record, Long> EXCHANGE_ID = createField(DSL.name("exchange_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column
     * <code>public.exchange_confirmation_status.target_user_id</code>.
     */
    public final TableField<Record, Long> TARGET_USER_ID = createField(DSL.name("target_user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column
     * <code>public.exchange_confirmation_status.source_user_id</code>.
     */
    public final TableField<Record, Long> SOURCE_USER_ID = createField(DSL.name("source_user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column
     * <code>public.exchange_confirmation_status.target_confirmed</code>.
     */
    public final TableField<Record, Boolean> TARGET_CONFIRMED = createField(DSL.name("target_confirmed"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column
     * <code>public.exchange_confirmation_status.source_confirmed</code>.
     */
    public final TableField<Record, Boolean> SOURCE_CONFIRMED = createField(DSL.name("source_confirmed"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.exchange_confirmation_status.event_id</code>.
     */
    public final TableField<Record, Long> EVENT_ID = createField(DSL.name("event_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.exchange_confirmation_status.expires_on</code>.
     */
    public final TableField<Record, Long> EXPIRES_ON = createField(DSL.name("expires_on"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.exchange_confirmation_status.action_type</code>.
     */
    public final TableField<Record, Short> ACTION_TYPE = createField(DSL.name("action_type"), SQLDataType.SMALLINT.nullable(false), this, "");

    private ExchangeConfirmationStatus(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private ExchangeConfirmationStatus(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.exchange_confirmation_status</code> table
     * reference
     */
    public ExchangeConfirmationStatus(String alias) {
        this(DSL.name(alias), EXCHANGE_CONFIRMATION_STATUS);
    }

    /**
     * Create an aliased <code>public.exchange_confirmation_status</code> table
     * reference
     */
    public ExchangeConfirmationStatus(Name alias) {
        this(alias, EXCHANGE_CONFIRMATION_STATUS);
    }

    /**
     * Create a <code>public.exchange_confirmation_status</code> table reference
     */
    public ExchangeConfirmationStatus() {
        this(DSL.name("exchange_confirmation_status"), null);
    }

    public <O extends Record> ExchangeConfirmationStatus(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, EXCHANGE_CONFIRMATION_STATUS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class ExchangeConfirmationStatusPath extends ExchangeConfirmationStatus implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> ExchangeConfirmationStatusPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private ExchangeConfirmationStatusPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public ExchangeConfirmationStatusPath as(String alias) {
            return new ExchangeConfirmationStatusPath(DSL.name(alias), this);
        }

        @Override
        public ExchangeConfirmationStatusPath as(Name alias) {
            return new ExchangeConfirmationStatusPath(alias, this);
        }

        @Override
        public ExchangeConfirmationStatusPath as(Table<?> alias) {
            return new ExchangeConfirmationStatusPath(alias.getQualifiedName(), this);
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
        return Keys.EXCHANGE_CONFIRMATION_STATUS_PKEY;
    }

    @Override
    public List<UniqueKey<Record>> getUniqueKeys() {
        return Arrays.asList(Keys.ONLY_ONE_CONCURRENT_EXCHANGE_PER_EVENT);
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_EVENT_FK, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_SOURCE_USER_FK, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_TARGET_USER_FK);
    }

    private transient EventsPath _events;

    /**
     * Get the implicit join path to the <code>public.events</code> table.
     */
    public EventsPath events() {
        if (_events == null)
            _events = new EventsPath(this, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_EVENT_FK, null);

        return _events;
    }

    private transient UsersPath _exchangeConfirmationStatusSourceUserFk;

    /**
     * Get the implicit join path to the <code>public.users</code> table, via
     * the <code>exchange_confirmation_status_source_user_fk</code> key.
     */
    public UsersPath exchangeConfirmationStatusSourceUserFk() {
        if (_exchangeConfirmationStatusSourceUserFk == null)
            _exchangeConfirmationStatusSourceUserFk = new UsersPath(this, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_SOURCE_USER_FK, null);

        return _exchangeConfirmationStatusSourceUserFk;
    }

    private transient UsersPath _exchangeConfirmationStatusTargetUserFk;

    /**
     * Get the implicit join path to the <code>public.users</code> table, via
     * the <code>exchange_confirmation_status_target_user_fk</code> key.
     */
    public UsersPath exchangeConfirmationStatusTargetUserFk() {
        if (_exchangeConfirmationStatusTargetUserFk == null)
            _exchangeConfirmationStatusTargetUserFk = new UsersPath(this, Keys.EXCHANGE_CONFIRMATION_STATUS__EXCHANGE_CONFIRMATION_STATUS_TARGET_USER_FK, null);

        return _exchangeConfirmationStatusTargetUserFk;
    }

    @Override
    public List<Check<Record>> getChecks() {
        return Arrays.asList(
            Internal.createCheck(this, DSL.name("exchange_confirmation_status_action_check"), "(((action_type >= 0) AND (action_type <= 1)))", true)
        );
    }

    @Override
    public ExchangeConfirmationStatus as(String alias) {
        return new ExchangeConfirmationStatus(DSL.name(alias), this);
    }

    @Override
    public ExchangeConfirmationStatus as(Name alias) {
        return new ExchangeConfirmationStatus(alias, this);
    }

    @Override
    public ExchangeConfirmationStatus as(Table<?> alias) {
        return new ExchangeConfirmationStatus(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeConfirmationStatus rename(String name) {
        return new ExchangeConfirmationStatus(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeConfirmationStatus rename(Name name) {
        return new ExchangeConfirmationStatus(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeConfirmationStatus rename(Table<?> name) {
        return new ExchangeConfirmationStatus(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus where(Condition condition) {
        return new ExchangeConfirmationStatus(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ExchangeConfirmationStatus where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ExchangeConfirmationStatus where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ExchangeConfirmationStatus where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public ExchangeConfirmationStatus where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public ExchangeConfirmationStatus whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}