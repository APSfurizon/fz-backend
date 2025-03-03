package net.furizon.backend.feature.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.CapabilitiesResponse;
import net.furizon.backend.feature.admin.dto.GenerateBadgeRequest;
import net.furizon.backend.feature.admin.usecase.export.GenerateBadgesHtmlUseCase;
import net.furizon.backend.feature.admin.usecase.GetCapabilitiesUseCase;
import net.furizon.backend.feature.admin.usecase.export.ExportHotelUseCase;
import net.furizon.backend.feature.admin.usecase.reminders.FursuitBadgeReminderUseCase;
import net.furizon.backend.feature.admin.usecase.reminders.OrderLinkReminderUseCase;
import net.furizon.backend.feature.admin.usecase.reminders.UserBadgeReminderUseCase;
import net.furizon.backend.infrastructure.media.DeleteMediaCronjob;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.media.usecase.RemoveDanglingMediaUseCase;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @org.jetbrains.annotations.NotNull
    private final DeleteMediaFromDiskAction deleteMediaAction;
    @org.jetbrains.annotations.NotNull
    private final DeleteMediaCronjob deleteMediaCronjob;

    @org.jetbrains.annotations.NotNull
    private final PretixConfig pretixConfig;
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;

    /*
   _____ _______ _    _ ______ ______
  / ____|__   __| |  | |  ____|  ____|
 | (___    | |  | |  | | |__  | |__
  \___ \   | |  | |  | |  __| |  __|
  ____) |  | |  | |__| | |    | |
 |_____/   |_|   \____/|_|    |_|
     */

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/countdown")
    public String countdown() {
        OffsetDateTime bookingStart = pretixConfig.getEvent().getPublicBookingStartTime();
        Duration remaining = Duration.between(bookingStart, OffsetDateTime.now());
        return "Start time: " + bookingStart + "; Remaining time: " + remaining;
    }

    @Operation(summary = "Gets what an user can do in the admin panel", description =
        "This method should be used to display or not buttons in the admin panel of the "
        + "reserved area")
    @PermissionRequired(permissions = {Permission.CAN_SEE_ADMIN_PAGES})
    @GetMapping("/capabilities")
    public CapabilitiesResponse getCapabilities(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(GetCapabilitiesUseCase.class, user);
    }

    /*
  _____  ______ __  __ _____ _   _ _____  ______ _____   _____
 |  __ \|  ____|  \/  |_   _| \ | |  __ \|  ____|  __ \ / ____|
 | |__) | |__  | \  / | | | |  \| | |  | | |__  | |__) | (___
 |  _  /|  __| | |\/| | | | | . ` | |  | |  __| |  _  / \___ \
 | | \ \| |____| |  | |_| |_| |\  | |__| | |____| | \ \ ____) |
 |_|  \_\______|_|  |_|_____|_| \_|_____/|______|_|  \_\_____/
     */

    @Operation(summary = "Remind user to link their orders", description =
        "Sends an email to all people who have made a paid order which is still unlinked with a link "
        + "they have to open for link the accounts")
    @PermissionRequired(permissions = {Permission.PRETIX_ADMIN})
    @GetMapping("/mail-reminders/order-linking")
    public void remindOrderLink(
        @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        int sent = executor.execute(OrderLinkReminderUseCase.class, pretixInformation);
        log.info("Sent {} order linking emails", sent);
    }

    @Operation(summary = "Remind user to upload their user badge", description =
        "Sends an email to all people who have made a paid order which have not set an user propic yet, "
            + "reminding them to do so")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @GetMapping("/mail-reminders/user-badge-upload")
    public void remindUserBadgeUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        int sent = executor.execute(UserBadgeReminderUseCase.class, pretixInformation.getCurrentEvent());
        log.info("Sent {} user badge upload emails", sent);
    }

    @Operation(summary = "Remind user to link their orders", description =
        "Sends an email to all people who have made a paid order which have not set a fursuit propic yet "
        + "for a fursuit they're bringing to the current event, reminding them to do so")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @GetMapping("/mail-reminders/fursuit-badge-upload")
    public void remindFursuitBadgeUpload(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        int sent = executor.execute(FursuitBadgeReminderUseCase.class, pretixInformation.getCurrentEvent());
        log.info("Sent {} fursuit badge upload emails", sent);
    }

    /*
  ________   _______   ____  _____ _______ _____
 |  ____\ \ / /  __ \ / __ \|  __ \__   __/ ____|
 | |__   \ V /| |__) | |  | | |__) | | | | (___
 |  __|   > < |  ___/| |  | |  _  /  | |  \___ \
 | |____ / . \| |    | |__| | | \ \  | |  ____) |
 |______/_/ \_\_|     \____/|_|  \_\ |_| |_____/
     */

    @Operation(summary = "Exports the list of user for the hotel")
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_USER_PUBLIC_INFO})
    @GetMapping(value = "/export/hotel-user-list")
    public ResponseEntity<String> exportHotel(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        log.info("User {} is exporting hotel users list", user.getUserId());
        String data = executor.execute(ExportHotelUseCase.class, pretixInformation);
        String fileName = "hotel-user-list-" + System.currentTimeMillis() + ".csv";
        return ResponseEntity.ok()
                             .contentType(new MediaType("text", "csv"))
                             .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                             .body(data);
    }

    @Operation(summary = "Generate the HTML page of the specified user badges", description =
        "By default this generates the badge for every user who comes to the current event. "
        + "Once you specify a search parameter, you will get only what you're searching for. "
        + "There are three search filter, they work in a AND manner (a badge is generated iff present in all of 3): "
        + "`orderCodes` a simple comma separated list of order codes. A badge for a user is generated only if it has "
        + "a order for the current event specified in the list. "
        + "`orderSerials` and `userIds` a comma separated list of _intervals_ of order serials/badge number and "
        + "user ids, expressed with a - sign. EG: 1,2,3-10,15,17-20,23. When the - sign is not present, the element "
        + "is NOT considered as an interval.")
    //@PermissionRequired(permissions = {Permission.PRETIX_ADMIN})
    @GetMapping("/export/badges/user")
    public ResponseEntity<String> generateUserBadges(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @Nullable final GenerateBadgeRequest request
    ) {
        String html = executor.execute(
                GenerateBadgesHtmlUseCase.class,
                new GenerateBadgesHtmlUseCase.Input(user, pretixInformation, request)
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.html")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    //TODO
    @Operation(summary = "Generate the HTML page of the specified fursuit badges", description =
            "Sends an email to all people who have made a paid order which is still unlinked with a link "
                    + "they have to open for link the accounts")
    //@PermissionRequired(permissions = {Permission.PRETIX_ADMIN})
    @GetMapping("/export/badges/fursuits")
    public ResponseEntity<String> generateFursuitBadges(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @Nullable final GenerateBadgeRequest request
    ) {
        String html = executor.execute(
                GenerateBadgesHtmlUseCase.class,
                new GenerateBadgesHtmlUseCase.Input(user, pretixInformation, request)
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.html")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /*
  __  __ ______ _____ _____           _____
 |  \/  |  ____|  __ \_   _|   /\    / ____|
 | \  / | |__  | |  | || |    /  \  | (___
 | |\/| |  __| | |  | || |   / /\ \  \___ \
 | |  | | |____| |__| || |_ / ____ \ ____) |
 |_|  |_|______|_____/_____/_/    \_\_____/
     */

    @PermissionRequired(permissions = {Permission.CAN_MANAGE_RAW_UPLOADS})
    @DeleteMapping("/media/")
    public boolean deleteMedias(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @RequestParam("id") Set<Long> ids
    ) {
        try {
            return deleteMediaAction.invoke(ids, true);
        } catch (IOException e) {
            log.error("Error while deleting medias", e);
            return false;
        }
    }

    @PermissionRequired(permissions = {Permission.CAN_MANAGE_RAW_UPLOADS})
    @PostMapping("/media/run-delete-media-cronjob")
    public void runDeleteMediaCronjob(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        long deleted = executor.execute(RemoveDanglingMediaUseCase.class, 0); //input is useless
        log.info("Deleted dangling {} medias", deleted);
    }
}
