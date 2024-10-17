package net.furizon.jooq.infrastructure.command;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.RowCountQuery;

import java.util.List;

public interface SqlCommand {
    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     */
    <Q extends RowCountQuery> int execute(Q query);

    /**
     * @throws org.jooq.exception.DataAccessException if something went wrong executing the query
     */
    @NotNull
    <R extends Record, Q extends ResultQuery<R>> List<R> executeResult(Q query);
}
