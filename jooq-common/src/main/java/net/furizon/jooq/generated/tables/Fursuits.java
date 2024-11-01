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
import net.furizon.jooq.generated.tables.FursuitsEvents.FursuitsEventsPath;
import net.furizon.jooq.generated.tables.Media.MediaPath;
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
public class Fursuits extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.fursuits</code>
     */
    public static final Fursuits FURSUITS = new Fursuits();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>public.fursuits.fursuit_id</code>.
     */
    public final TableField<Record, Long> FURSUIT_ID = createField(DSL.name("fursuit_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.fursuits.fursuit_name</code>.
     */
    public final TableField<Record, String> FURSUIT_NAME = createField(DSL.name("fursuit_name"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.fursuits.fursuit_species</code>.
     */
    public final TableField<Record, String> FURSUIT_SPECIES = createField(DSL.name("fursuit_species"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.fursuits.user_id</code>.
     */
    public final TableField<Record, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.fursuits.media_id_propic</code>.
     */
    public final TableField<Record, Long> MEDIA_ID_PROPIC = createField(DSL.name("media_id_propic"), SQLDataType.BIGINT, this, "");

    private Fursuits(Name alias, Table<Record> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Fursuits(Name alias, Table<Record> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.fursuits</code> table reference
     */
    public Fursuits(String alias) {
        this(DSL.name(alias), FURSUITS);
    }

    /**
     * Create an aliased <code>public.fursuits</code> table reference
     */
    public Fursuits(Name alias) {
        this(alias, FURSUITS);
    }

    /**
     * Create a <code>public.fursuits</code> table reference
     */
    public Fursuits() {
        this(DSL.name("fursuits"), null);
    }

    public <O extends Record> Fursuits(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
        super(path, childPath, parentPath, FURSUITS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class FursuitsPath extends Fursuits implements Path<Record> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> FursuitsPath(Table<O> path, ForeignKey<O, Record> childPath, InverseForeignKey<O, Record> parentPath) {
            super(path, childPath, parentPath);
        }
        private FursuitsPath(Name alias, Table<Record> aliased) {
            super(alias, aliased);
        }

        @Override
        public FursuitsPath as(String alias) {
            return new FursuitsPath(DSL.name(alias), this);
        }

        @Override
        public FursuitsPath as(Name alias) {
            return new FursuitsPath(alias, this);
        }

        @Override
        public FursuitsPath as(Table<?> alias) {
            return new FursuitsPath(alias.getQualifiedName(), this);
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
        return Keys.FURSUITS_PKEY;
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return Arrays.asList(Keys.FURSUITS__FURSUITS_MEDIA_FK, Keys.FURSUITS__FURSUITS_USERS_FK);
    }

    private transient MediaPath _media;

    /**
     * Get the implicit join path to the <code>public.media</code> table.
     */
    public MediaPath media() {
        if (_media == null)
            _media = new MediaPath(this, Keys.FURSUITS__FURSUITS_MEDIA_FK, null);

        return _media;
    }

    private transient UsersPath _users;

    /**
     * Get the implicit join path to the <code>public.users</code> table.
     */
    public UsersPath users() {
        if (_users == null)
            _users = new UsersPath(this, Keys.FURSUITS__FURSUITS_USERS_FK, null);

        return _users;
    }

    private transient FursuitsEventsPath _fursuitsEvents;

    /**
     * Get the implicit to-many join path to the
     * <code>public.fursuits_events</code> table
     */
    public FursuitsEventsPath fursuitsEvents() {
        if (_fursuitsEvents == null)
            _fursuitsEvents = new FursuitsEventsPath(this, null, Keys.FURSUITS_EVENTS__FURSUITS_EVENTS_FURSUIT_FK.getInverseKey());

        return _fursuitsEvents;
    }

    @Override
    public Fursuits as(String alias) {
        return new Fursuits(DSL.name(alias), this);
    }

    @Override
    public Fursuits as(Name alias) {
        return new Fursuits(alias, this);
    }

    @Override
    public Fursuits as(Table<?> alias) {
        return new Fursuits(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Fursuits rename(String name) {
        return new Fursuits(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Fursuits rename(Name name) {
        return new Fursuits(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Fursuits rename(Table<?> name) {
        return new Fursuits(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits where(Condition condition) {
        return new Fursuits(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Fursuits where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Fursuits where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Fursuits where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Fursuits where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Fursuits whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
