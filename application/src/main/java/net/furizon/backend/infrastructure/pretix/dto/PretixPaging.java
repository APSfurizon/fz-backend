package net.furizon.backend.infrastructure.pretix.dto;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class PretixPaging<R> {
    public static final int DEFAULT_PAGE = 1;

    @JsonIgnore
    private final Pattern pageNumberPatter = Pattern.compile("page=(\\d+)");

    private final int page;

    private final int count;

    @Nullable
    private final String next;

    @Nullable
    private final String previous;

    // Can't find any info if is nullable
    @Nullable
    private final List<R> results;

    public boolean hasNext() {
        return next != null;
    }

    private int extractPageNumber(@Nullable String s) {
        if (s == null) {
            throw new NoSuchElementException("No more pages");
        }

        Matcher matcher = pageNumberPatter.matcher(s);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Couldn't find page from url");
        }

        return Integer.parseInt(matcher.group(1));
    }

    public int nextPage() {
        return extractPageNumber(next);
    }
    public int previousPage() {
        return extractPageNumber(previous);
    }

    public static <R> PretixPaging<R> empty() {
        return new PretixPaging<>(
            0,
            1,
            null,
            null,
            null
        );
    }
}
