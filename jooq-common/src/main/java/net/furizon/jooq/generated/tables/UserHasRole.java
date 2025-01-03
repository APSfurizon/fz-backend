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
import net.furizon.jooq.generated.tables.Role.RolePath;
import net.furizon.jooq.generated.tables.Users.UsersPath;

import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
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
public class UserHasRole extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.user_has_role</code>
     */
    public static final UserHasRole USER_HAS_ROLE = new UserHasRole();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.user_has_role.user_id</code>.
     */
    public final TableField<Record, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.user_has_role.role_id</code>.
     */
    public final TableField<Record, Long> ROLE_ID = createField(DSL.name("role_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private UserHasRole(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private UserHasRole(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.user_has_role</code> table reference
     */
    public UserHasRole(String alias) {
        this(DSL.name(alias), USER_HAS_ROLE);
    }

    /**
     * Create an aliased <code>public.user_has_role</code> table reference
     */
    public UserHasRole(Name alias) {
        this(alias, USER_HAS_ROLE);
    }

    /**
     * Create a <code>public.user_has_role</code> table reference
     */
    public UserHasRole() {
        this(DSL.name("user_has_role"), null);
    }

    public <O extends Record> UserHasRole(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, USER_HAS_ROLE);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UserHasRolePath extends UserHasRole implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> UserHasRolePath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private UserHasRolePath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public UserHasRolePath as(String alias) {
            return new UserHasRolePath(DSL.name(alias), this);
        }

        @Override
        public UserHasRolePath as(Name alias) {
            return new UserHasRolePath(alias, this);
        }

        @Override
        public UserHasRolePath as(Table<?> alias) {
            return new UserHasRolePath(alias.getQualifiedName(), this);
        }
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return Keys.USER_HAS_ROLE_PK;
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.USER_HAS_ROLE__USER_HAS_ROLE_ROLE_FK, Keys.USER_HAS_ROLE__USER_HAS_ROLE_USER_FK);
    }

    private transient RolePath _role;

    /**
     * Get the implicit join path to the <code>public.role</code> table.
     */
    public RolePath role() {
        if (_role == null)
            _role = new RolePath(this, Keys.USER_HAS_ROLE__USER_HAS_ROLE_ROLE_FK, null);

        return _role;
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.USER_HAS_ROLE__USER_HAS_ROLE_USER_FK, null);

        return _users;
    }

    @Override
    public UserHasRole as(String alias) {
        return new UserHasRole(DSL.name(alias), this);
    }

    @Override
    public UserHasRole as(Name alias) {
        return new UserHasRole(alias, this);
    }

    @Override
    public UserHasRole as(Table<?> alias) {
        return new UserHasRole(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public UserHasRole rename(String name) {
        return new UserHasRole(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserHasRole rename(Name name) {
        return new UserHasRole(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public UserHasRole rename(Table<?> name) {
        return new UserHasRole(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole where(Condition condition) {
        return new UserHasRole(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserHasRole where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserHasRole where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserHasRole where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public UserHasRole where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public UserHasRole whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}