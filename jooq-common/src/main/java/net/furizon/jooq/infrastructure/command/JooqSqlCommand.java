package net.furizon.jooq.infrastructure.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.RowCountQuery;

import java.util.List;

@RequiredArgsConstructor
public class JooqSqlCommand implements SqlCommand {
    private final DSLContext dslContext;

    @Override
    public <Q extends RowCountQuery> int execute(Q query) {
        query.attach(dslContext.configuration());
        return query.execute();
    }

    @NotNull
    @Override
    public <R extends Record, Q extends ResultQuery<R>> List<R> executeResult(Q query) {
        query.attach(dslContext.configuration());
        return query.fetch();
    }
}
