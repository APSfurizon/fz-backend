package net.furizon.backend.infrastructure.pretix;

import net.furizon.backend.infrastructure.pretix.dto.PretixPaging;
import org.springframework.data.util.Pair;

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

            consumer.accept(Pair.of(response.getResults(), response));

            hasNext = response.hasNext();
            if (hasNext) {
                currentPage = response.nextPage();
            }
        }
    }
}
