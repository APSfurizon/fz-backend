package net.furizon.jooq.infrastructure;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

import java.util.function.Function;

@RequiredArgsConstructor
public class JooqOptional<R extends Record> {
    @Nullable
    private final R record;

    public boolean isPresent() {
        return record != null;
    }

    @Nullable
    public R get() {
        return record;
    }

    @Nullable
    public <U> U mapOrNull(Function<R, ? extends U> mapper) {
        if (!isPresent()) {
            return null;
        }

        return mapper.apply(record);
    }

    @NotNull
    public static <R extends Record> JooqOptional<R> of(@Nullable R record) {
        return new JooqOptional<>(record);
    }
}
