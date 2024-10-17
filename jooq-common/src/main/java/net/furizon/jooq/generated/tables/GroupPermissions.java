/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated.tables;


import java.util.Collection;

import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.Keys;
import net.furizon.jooq.generated.Public;

import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
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
public class GroupPermissions extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.group_permissions</code>
     */
    public static final GroupPermissions GROUP_PERMISSIONS = new GroupPermissions();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.group_permissions.group_id</code>.
     */
    public final TableField<Record, Long> GROUP_ID = createField(DSL.name("group_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.group_permissions.permission_code</code>.
     */
    public final TableField<Record, String> PERMISSION_CODE = createField(DSL.name("permission_code"), SQLDataType.CLOB.nullable(false), this, "");

    private GroupPermissions(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private GroupPermissions(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.group_permissions</code> table reference
     */
    public GroupPermissions(String alias) {
        this(DSL.name(alias), GROUP_PERMISSIONS);
    }

    /**
     * Create an aliased <code>public.group_permissions</code> table reference
     */
    public GroupPermissions(Name alias) {
        this(alias, GROUP_PERMISSIONS);
    }

    /**
     * Create a <code>public.group_permissions</code> table reference
     */
    public GroupPermissions() {
        this(DSL.name("group_permissions"), null);
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return Keys.GROUP_PERMISSIONS_PK;
    }

    @Override
    public GroupPermissions as(String alias) {
        return new GroupPermissions(DSL.name(alias), this);
    }

    @Override
    public GroupPermissions as(Name alias) {
        return new GroupPermissions(alias, this);
    }

    @Override
    public GroupPermissions as(Table<?> alias) {
        return new GroupPermissions(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public GroupPermissions rename(String name) {
        return new GroupPermissions(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public GroupPermissions rename(Name name) {
        return new GroupPermissions(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public GroupPermissions rename(Table<?> name) {
        return new GroupPermissions(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions where(Condition condition) {
        return new GroupPermissions(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public GroupPermissions where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public GroupPermissions where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public GroupPermissions where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public GroupPermissions where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public GroupPermissions whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}