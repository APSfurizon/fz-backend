package net.furizon.backend.feature.pretix.objects.states.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.states.dto.PretixCountryResponse;
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

    @Operation(summary = "Get country states of a specified ISO 3166-1 A-2 country", description =
        "Receives in input a ISO 3166-1 country and returns a list of provinces/regions/states for that given country. "
        + "This has to match at least the list of states exposed by pretix (this is guaranteed internally), "
        + "this is the reason we support only a small amount (the necessary one) of states + italy. "
        + "If an empty array is returned, it means that the state is unsupported. Due to italian laws, "
        + "we still have to ask the user for a state. In that instance, we can use a normal text field in the "
        + "frontend. Chosen country should be sent back to the backend as ISO 3166 A-2")
    @GetMapping("/by-country")
    public PretixStateResponse getPretixStates(@Valid
                                             @NotNull
                                             @Size(min = 2)
                                             @RequestParam("code")
                                             final String code) {
        var states = pretixInformation.getStatesOfCountry(code);
        return new PretixStateResponse(states);
    }

    @Operation(summary = "Gets a list of countries with the international phone prefix", description =
        "Chosen country should be sent back to the backend as ISO 3166-1 A-2")
    @GetMapping("/get-countries")
    public PretixCountryResponse getCountries() {
        var countries = pretixInformation.getStatesOfCountry(Const.ALL_COUNTRIES_STATE_KEY);
        return new PretixCountryResponse(countries);
    }
}
