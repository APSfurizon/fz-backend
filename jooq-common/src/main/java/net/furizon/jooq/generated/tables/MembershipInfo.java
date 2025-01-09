/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated.tables;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.Keys;
import net.furizon.jooq.generated.Public;
import net.furizon.jooq.generated.tables.Events.EventsPath;
import net.furizon.jooq.generated.tables.Users.UsersPath;

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
public class MembershipInfo extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.membership_info</code>
     */
    public static final MembershipInfo MEMBERSHIP_INFO = new MembershipInfo();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.membership_info.id</code>.
     */
    public final TableField<Record, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.membership_info.user_id</code>.
     */
    public final TableField<Record, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_first_name</code>.
     */
    public final TableField<Record, String> INFO_FIRST_NAME = createField(DSL.name("info_first_name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_last_name</code>.
     */
    public final TableField<Record, String> INFO_LAST_NAME = createField(DSL.name("info_last_name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_fiscal_code</code>.
     */
    public final TableField<Record, String> INFO_FISCAL_CODE = createField(DSL.name("info_fiscal_code"), SQLDataType.VARCHAR(16), this, "");

    /**
     * The column <code>public.membership_info.info_birth_city</code>.
     */
    public final TableField<Record, String> INFO_BIRTH_CITY = createField(DSL.name("info_birth_city"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_birth_region</code>.
     */
    public final TableField<Record, String> INFO_BIRTH_REGION = createField(DSL.name("info_birth_region"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.membership_info.info_birth_country</code>.
     */
    public final TableField<Record, String> INFO_BIRTH_COUNTRY = createField(DSL.name("info_birth_country"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_birthday</code>.
     */
    public final TableField<Record, LocalDate> INFO_BIRTHDAY = createField(DSL.name("info_birthday"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_address</code>.
     */
    public final TableField<Record, String> INFO_ADDRESS = createField(DSL.name("info_address"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_zip</code>.
     */
    public final TableField<Record, String> INFO_ZIP = createField(DSL.name("info_zip"), SQLDataType.VARCHAR(16).nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_city</code>.
     */
    public final TableField<Record, String> INFO_CITY = createField(DSL.name("info_city"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_region</code>.
     */
    public final TableField<Record, String> INFO_REGION = createField(DSL.name("info_region"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.membership_info.info_country</code>.
     */
    public final TableField<Record, String> INFO_COUNTRY = createField(DSL.name("info_country"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_phone</code>.
     */
    public final TableField<Record, String> INFO_PHONE = createField(DSL.name("info_phone"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_phone_prefix</code>.
     */
    public final TableField<Record, String> INFO_PHONE_PREFIX = createField(DSL.name("info_phone_prefix"), SQLDataType.VARCHAR(8).nullable(false), this, "");

    /**
     * The column <code>public.membership_info.info_allergies</code>.
     */
    public final TableField<Record, String> INFO_ALLERGIES = createField(DSL.name("info_allergies"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.membership_info.last_updated_event_id</code>.
     */
    public final TableField<Record, Long> LAST_UPDATED_EVENT_ID = createField(DSL.name("last_updated_event_id"), SQLDataType.BIGINT, this, "");

    private MembershipInfo(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private MembershipInfo(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.membership_info</code> table reference
     */
    public MembershipInfo(String alias) {
        this(DSL.name(alias), MEMBERSHIP_INFO);
    }

    /**
     * Create an aliased <code>public.membership_info</code> table reference
     */
    public MembershipInfo(Name alias) {
        this(alias, MEMBERSHIP_INFO);
    }

    /**
     * Create a <code>public.membership_info</code> table reference
     */
    public MembershipInfo() {
        this(DSL.name("membership_info"), null);
    }

    public <O extends Record> MembershipInfo(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, MEMBERSHIP_INFO);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class MembershipInfoPath extends MembershipInfo implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> MembershipInfoPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private MembershipInfoPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public MembershipInfoPath as(String alias) {
            return new MembershipInfoPath(DSL.name(alias), this);
        }

        @Override
        public MembershipInfoPath as(Name alias) {
            return new MembershipInfoPath(alias, this);
        }

        @Override
        public MembershipInfoPath as(Table<?> alias) {
            return new MembershipInfoPath(alias.getQualifiedName(), this);
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
        return Keys.MEMBERSHIP_INFO_ID_PKEY;
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_UPDATED_EVENT_ID, Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_USERS_FK);
    }

    private transient EventsPath _events;

    /**
     * Get the implicit join path to the <code>public.events</code> table.
     */
    public EventsPath events() {
        if (_events == null)
            _events = new EventsPath(this, Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_UPDATED_EVENT_ID, null);

        return _events;
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.MEMBERSHIP_INFO__MEMBERSHIP_INFO_USERS_FK, null);

        return _users;
    }

    @Override
    public MembershipInfo as(String alias) {
        return new MembershipInfo(DSL.name(alias), this);
    }

    @Override
    public MembershipInfo as(Name alias) {
        return new MembershipInfo(alias, this);
    }

    @Override
    public MembershipInfo as(Table<?> alias) {
        return new MembershipInfo(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public MembershipInfo rename(String name) {
        return new MembershipInfo(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MembershipInfo rename(Name name) {
        return new MembershipInfo(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public MembershipInfo rename(Table<?> name) {
        return new MembershipInfo(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo where(Condition condition) {
        return new MembershipInfo(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public MembershipInfo where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public MembershipInfo where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public MembershipInfo where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public MembershipInfo where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public MembershipInfo whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
