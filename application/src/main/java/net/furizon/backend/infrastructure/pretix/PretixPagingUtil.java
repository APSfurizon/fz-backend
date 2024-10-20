package net.furizon.backend.infrastructure.pretix;

import kotlin.Pair;
import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PretixPagingUtil {
    public static <U, P extends PretixPaging<U>> void fetchAll(
        Function<Integer, P> callable,
        Consumer<Pair<List<U>, P>> consumer
    ) {
        var currentPage = PretixPaging.DEFAULT_PAGE;
        boolean hasNext = true;
        while (hasNext) {
            final P response = callable.apply(currentPage);
            if (response.getResults() == null) {
                continue;
            }

            consumer.accept(new Pair<>(response.getResults(), response));

            hasNext = response.hasNext();
            if (hasNext) {
                currentPage = response.nextPage();
            }
        }
    }
}
