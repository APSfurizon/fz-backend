package net.furizon.jooq.infrastructure.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.Select;

import java.util.List;

public interface SqlQuery {
    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     */
    @NotNull
    <R extends Record, Q extends Select<R>> List<R> fetch(Q query);

    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     */
    <R extends Record, Q extends Select<R>> int count(Q query);

    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     * @throws org.jooq.exception.TooManyRowsException if the query returned more than one record
     * @throws org.jooq.exception.TooManyRowsException if the query returned more than one record
     */
    @NotNull
    <R extends Record, Q extends Select<R>> R fetchSingle(Q query);

    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     */
    @Nullable
    <R extends Record, Q extends Select<R>> R fetchFirst(Q query);
}
