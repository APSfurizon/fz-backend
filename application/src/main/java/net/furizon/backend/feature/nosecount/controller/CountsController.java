package net.furizon.backend.feature.nosecount.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.FursuitCountResponse;
import net.furizon.backend.feature.nosecount.finder.CountsFinder;
import net.furizon.backend.feature.nosecount.usecase.LoadFursuitCountUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counts")
@RequiredArgsConstructor
public class CountsController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;

    @Operation(summary = "Gets the fursuit count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the fursuit count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only the fursuits brought to that event "
        + "with the `displayInNosecount` check will be returned by this method")
    @GetMapping("/fursuit")
    public FursuitCountResponse getFursuitCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }
        return executor.execute(
                LoadFursuitCountUseCase.class,
                eventId
        );
    }

    @GetMapping("/bopos")
    public void getNosecount() {
    }
}
