package net.furizon.backend.feature.pretix.objects.states.finder;

import net.furizon.backend.feature.pretix.objects.states.PretixState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PretixStateFinder {
    @NotNull
    List<PretixState> getPretixStates(@NotNull String countryCode);

    //Italy is not directly supported by pretix, so we have to get
    //these from a random github. We add support specifically for
    //italy, because.... yeah, most of furizon's attendees are it
    @NotNull
    List<PretixState> getItalianStates();

    @NotNull
    List<PretixState> getCountries();
}
