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

    @Nullable
    private final String next;

    @Nullable
    private final String prev;

    // Can't find any info if is nullable
    @Nullable
    private final List<R> results;

    public boolean hasNext() {
        return next != null;
    }

    public int nextPage() {
        if (next == null) {
            throw new NoSuchElementException("No more pages");
        }

        Matcher matcher = pageNumberPatter.matcher(next);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Couldn't find page from url");
        }

        return Integer.parseInt(matcher.group(1));
    }

    public static <R> PretixPaging<R> empty() {
        return new PretixPaging<>(
            0,
            null,
            null,
            null
        );
    }
}
