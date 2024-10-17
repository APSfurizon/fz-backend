package net.furizon.jooq.infrastructure.query;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.JooqOptional;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JooqSqlQuery implements SqlQuery {
    private final DSLContext dslContext;

    @NotNull
    @Override
    public <R extends Record, Q extends Select<R>> List<R> fetch(Q query) {
        query.attach(dslContext.configuration());
        return query.fetch();
    }

    @Override
    public <R extends Record, Q extends Select<R>> int count(Q query) {
        return Optional
            .ofNullable(
                dslContext.selectCount()
                    .from(query)
                    .fetchSingle(DSL.count())
            )
            .orElse(0);
    }

    @NotNull
    @Override
    public <R extends Record, Q extends Select<R>> R fetchSingle(Q query) {
        query.attach(dslContext.configuration());
        return query.fetchSingle();
    }

    @NotNull
    @Override
    public <R extends Record, Q extends Select<R>> JooqOptional<R> fetchFirst(Q query) {
        query.attach(dslContext.configuration());
        return JooqOptional.of(query.fetchAny());
    }
}
