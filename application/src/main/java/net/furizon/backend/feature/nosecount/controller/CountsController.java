package net.furizon.backend.feature.nosecount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.PermanentCounts;
import net.furizon.backend.feature.nosecount.dto.responses.AdminCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.FursuitCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.NoseCountResponse;
import net.furizon.backend.feature.nosecount.dto.responses.SponsorCountResponse;
import net.furizon.backend.feature.nosecount.usecase.LoadAdminCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadFursuitCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadNoseCountUseCase;
import net.furizon.backend.feature.nosecount.usecase.LoadSponsorCountUseCase;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.event.usecase.GetSponsorshipNamesUseCase;
import net.furizon.backend.infrastructure.nosecount.NosecountConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/v1/counts")
@RequiredArgsConstructor
public class CountsController {
    private final PretixInformation pretixInformation;
    private final UseCaseExecutor executor;
    private final EventFinder eventFinder;

    @NotNull
    private final NosecountConfig nosecountConfig;
    @NotNull
    private final ObjectMapper objectMapper;

    private final Map<Long, FursuitCountResponse> permanentFursuitCount = new TreeMap<>();

    @Operation(summary = "Gets the fursuit count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the fursuit count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only the fursuits brought to that event "
        + "with the `displayInNosecount` check will be returned by this method. "
        + "Some people may set their fursuit as publicly owned by them. In that instance "
        + "the field `ownerId` will be populated with the userId of the owner of the fursuit.")
    @GetMapping("/fursuit")
    public FursuitCountResponse getFursuitCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }

        var resp = permanentFursuitCount.get(eventId);
        if (resp != null) {
            return resp;
        }

        return executor.execute(
                LoadFursuitCountUseCase.class,
                eventId
        );
    }

    private final Map<Long, SponsorCountResponse> permanentSponsorCount = new TreeMap<>();

    @Operation(summary = "Gets the sponsor count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the sponsor count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only users with a sponsorship level "
        + "> NONE will be displayed from this endpoint. The sponsor names displayed should "
        + "be the ones returned in the sponsorNames field")
    @GetMapping("/sponsors")
    public SponsorCountResponse getSponsorCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }

        SponsorCountResponse resp = permanentSponsorCount.get(eventId);
        if (resp != null) {
            return resp; //Already contains sponsor names
        }

        resp = executor.execute(
                LoadSponsorCountUseCase.class,
                eventId
        );
        resp.setSponsorNames(
                executor.execute(
                        GetSponsorshipNamesUseCase.class,
                        new GetSponsorshipNamesUseCase.Input(eventId, pretixInformation)
                )
        );
        return resp;
    }

    private final Map<Long, NoseCountResponse> permanentBopoCount = new TreeMap<>();

    @Operation(summary = "Gets the nose count", description =
        "By using the optional paramether `event-id` you can choose of which event "
        + "you can fetch the nose count. If you leave it blank or null, you will "
        + "obtain the one for the current event. Only users with the `showInNosecount` "
        + "tick set to true will be displayed from this endpoint")
    @GetMapping("/bopos")
    public NoseCountResponse getNosecount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        Event event;
        if (eventId == null) {
            event = pretixInformation.getCurrentEvent();
            NoseCountResponse resp = permanentBopoCount.get(event.getId());
            if (resp != null) {
                return resp;
            }
        } else {
            NoseCountResponse resp = permanentBopoCount.get(eventId);
            if (resp != null) {
                return resp;
            }
            event = eventFinder.findEventById(eventId);
        }
        return executor.execute(
                LoadNoseCountUseCase.class,
                new LoadNoseCountUseCase.Input(
                        event,
                        pretixInformation
                )
        );
    }

    private final Map<Long, AdminCountResponse> permanentAdminCount = new TreeMap<>();

    @Operation(summary = "Gets the admin count", description =
        "This method DOES NOT returns only the admins coming to the current event, "
        + "but ALL users which are in a role noted as 'show in admin count'")
    @GetMapping("/admins")
    public AdminCountResponse getAdminCount(
            @RequestParam(value = "event-id", required = false) @Valid @Nullable Long eventId
    ) {
        if (eventId == null) {
            eventId = pretixInformation.getCurrentEvent().getId();
        }

        AdminCountResponse resp = permanentAdminCount.get(eventId);
        if (resp != null) {
            return resp;
        }

        return executor.execute(LoadAdminCountUseCase.class, 0);
    }

    private static final String NAME_PREFIX = "count_";
    private static final String NAME_SUFFIX = ".json";

    @PostConstruct
    public void initPermanentCounts() {
        log.info("Loading permanent nosecount(s)");
        var conf = nosecountConfig.getPermanent();
        if (conf == null || conf.getJsonPath() == null || conf.getJsonPath().isEmpty()) {
            log.debug("Permanent nosecount is not enabled");
            return;
        }

        Path path = Paths.get(conf.getJsonPath());
        if (Files.notExists(path)) {
            log.warn("Permanent nosecount dir {} does not exists", path);
            return;
        }

        log.debug("Loading permanent nosecount from dir {}", path);
        try (Stream<Path> stream = Files.list(path)) {
            stream
                .filter(file -> !Files.isDirectory(file))
                .filter(file -> {
                    String fileName = FilenameUtils.getName(file.getFileName().toString());
                    return fileName.startsWith(NAME_PREFIX) && fileName.endsWith(NAME_SUFFIX);
                })
                .forEach(file -> {
                    String fileName = FilenameUtils.getName(file.getFileName().toString());
                    long eventId = Long.parseLong(
                            fileName.substring(
                                    NAME_PREFIX.length(),
                                    fileName.length() - NAME_SUFFIX.length()
                            )
                    );
                    try {
                        PermanentCounts data = objectMapper.readValue(Files.readAllBytes(file), PermanentCounts.class);
                        permanentAdminCount.put(eventId, data.getAdmins());
                        permanentBopoCount.put(eventId, data.getBopos());
                        permanentSponsorCount.put(eventId, data.getSponsors());
                        permanentFursuitCount.put(eventId, data.getFursuits());
                        log.info("Loaded permanent nosecount for event {} from file {}", eventId, file.getFileName());
                    } catch (IOException e) {
                        log.error("Error while reading permanent nosecount file {}", file, e);
                    }
                });
        } catch (IOException e) {
            log.error("Error while reading permanent nosecount dir {}", path, e);
        }
    }
}
