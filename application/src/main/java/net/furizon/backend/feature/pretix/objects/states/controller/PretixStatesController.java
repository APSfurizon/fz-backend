package net.furizon.backend.feature.pretix.objects.states.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
public class PretixStatesController {
    private final PretixInformation pretixInformation;

    @GetMapping("/by-country")
    public List<PretixState> getPretixStates(@Valid
                                             @NotNull
                                             @Size(min = 2)
                                             @RequestParam("code")
                                             final Optional<String> code) {
        return code.map(pretixInformation::getStatesOfCountry).orElseGet(LinkedList::new);
    }

    @GetMapping("/get-countries")
    public List<PretixState> getCountries() {
        return pretixInformation.getStatesOfCountry(Const.ALL_COUNTRIES_STATE_KEY);
    }
}
