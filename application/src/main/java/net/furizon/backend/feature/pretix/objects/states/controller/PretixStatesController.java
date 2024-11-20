package net.furizon.backend.feature.pretix.objects.states.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.states.dto.PretixStateResponse;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
public class PretixStatesController {
    private final PretixInformation pretixInformation;

    @GetMapping("/by-country")
    public PretixStateResponse getPretixStates(@Valid
                                             @NotNull
                                             @Size(min = 2)
                                             @RequestParam("code")
                                             final String code) {
        var states = pretixInformation.getStatesOfCountry(code);
        return new PretixStateResponse(states);
    }

    @GetMapping("/get-countries")
    public PretixStateResponse getCountries() {
        var countries = pretixInformation.getStatesOfCountry(Const.ALL_COUNTRIES_STATE_KEY);
        return new PretixStateResponse(countries);
    }
}
