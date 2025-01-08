package net.furizon.backend.feature.pretix.objects.states.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.feature.pretix.objects.states.finder.PretixStateFinder;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchStatesByCountry implements UseCase<String, List<PretixState>> {
    @NotNull
    private final PretixStateFinder stateFinder;

    @NotNull
    @Override
    public List<PretixState> executor(@NotNull String countryIsoCode) {
        List<PretixState> ret;
        if (countryIsoCode.equalsIgnoreCase("IT")) {
            ret = stateFinder.getItalianStates();
        } else if (countryIsoCode.equalsIgnoreCase(PretixConst.ALL_COUNTRIES_STATE_KEY)) {
            ret = stateFinder.getCountries();
        } else {
            ret = stateFinder.getPretixStates(countryIsoCode);
        }
        return ret;
    }
}
