package net.furizon.backend.feature.gallery.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.response.*;
import net.furizon.backend.feature.gallery.usecase.*;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gallery")
@RequiredArgsConstructor
public class GalleryController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;


    @Operation(summary = "Retrieves the full data of the specified upload", description =
        "This method is intended to be used to display the modal of a specific upload. "
        + "It returns all the data needed to visualize it. A bit of more info on some of the "
        + "returned fields: you can ignore `originalUploader`, it's for debugging purpose. "
        + "`uploadDate` is when the user has completed the upload, while `shotDate` may contain "
        + "the date when the photo or video was actually taken (if we're able to extract it). "
        + "`status` represent the admin approval process, only an admin or the photographer can see "
        + "photos in pending or rejected status. By looking at `type` you can understand if this is a "
        + "photo or a video. `displayMedia` contains the media object representing the lightweight *IMAGE* (and "
        + "this is guaranteed to ALWAYS be a displayable image!) to be shown in the modal. Instead `downloadMedia` "
        + "is the media an user should download with the appropriate button, keeping in mind that its name as "
        + "stored on S3 is not the original one; frontend should rename the file when downloading it using what's in "
        + "the `fileName` field. Finally, `thumbnailMedia` contains the small square thumbnail you should display as "
        + "preview. Keep in mind that post processing is done async, so immediately after an user has uploaded a "
        + "new media, this field will be null and you should display a temporary icon instead. If an user is an "
        + "admin, he can mark a media as 'selected' for an event, making it the photo cover of the event. "
        + "`isSelected` tells you exactly if this is the selected photo for the event. `repostPermissions` is "
        + "a banner you should display on the page, telling the users if and how they're allowed by the original "
        + "photographer to repost their image around. `photoMetadata` and `videoMetadata` contains extra information "
        + "for both photos and videos. They might be both null if we're unable to fetch that extra information from "
        + "the file. Each field might be null, if, again, we cannot retrieve the information from the media.")
    @GetMapping("/pub/{uploadId}")
    public @NotNull GalleryUpload getUpload(
        @PathVariable @NotNull @Valid @Positive final Long uploadId,
        @AuthenticationPrincipal @Valid @Nullable final FurizonUser user
    ) {
        return executor.execute(
                FetchUploadUseCase.class,
                new FetchUploadUseCase.Input(
                        uploadId,
                        user
                )
        );
    }

    @Operation(summary = "List, filter and search all uploads", description =
        "This endpoint is meant to create the page with the list of uploads, where an "
        + "user will later click on one of them and it opens up (by calling `GET /pub/{uploadId}`). "
        + "The objects returned here are smaller than the one for the exact upload fetch, they only "
        + "contain few parameters, which are all explained in the previously mentioned method. "
        + "Keep in mind that since post processing is async, a newly uploaded media may not have "
        + "a thumbnail yet, so the reference could be null. In that instance, frontend should "
        + "be display a default icon.")
    @GetMapping("/pub/list")
    public @NotNull ListUploadsResponse listUploads(
            @RequestParam @Nullable @Valid @Positive final Long photographerUserId,
            @RequestParam @Nullable @Valid @Positive final Long eventId,
            @RequestParam @Nullable @Valid @PositiveOrZero final Long fromUploadId,
            @AuthenticationPrincipal @Valid @Nullable final FurizonUser user
    ) {
        return executor.execute(
                ListUploadsUseCase.class,
                new ListUploadsUseCase.Input(
                        fromUploadId,
                        photographerUserId,
                        eventId,
                        user
                )
        );
    }

    @Operation(summary = "List all the events with photos, with extra gallery-related information", description =
        "This endpoint should be used to display a list of events with their small gallery card. "
        + "Together with the event, this returns the number of photos per each event, and the medias "
        + "which have been selected as cover for this event. You have two sizes of the media, "
        + "a square small thumbnail and a bigger one. Use it depending on your needs. "
        + "Keep in mind that there may be no cover photo selected at all, so the frontend should fallback "
        + "to a default icon if neither of the twos are present. By specifying the query param `photographerUserId` "
        + "you can filter the results to just the events a photographer has uploaded to, and the counts will be only "
        + "on his photos.")
    @GetMapping("/pub/events")
    public @NotNull ListGalleryEventsResponse listEvents(
        @RequestParam @Nullable @Valid @Positive final Long photographerUserId
    ) {
        return executor.execute(
            ListGalleryEventsUseCase.class,
            photographerUserId == null ? -1L : photographerUserId
        );
    }

    @Operation(summary = "Fetch the specified event, together with extra gallery-related information", description =
        "This endpoint behaves the same as `GET /pub/events`. Please look at his documentation. "
        + "The idea behind this single fetch is to have a big event card to show if a particular event is selected")
    @GetMapping("/pub/events/{eventId}")
    public @NotNull GalleryEvent listEvents(
            @RequestParam @Nullable @Valid @Positive final Long photographerUserId,
            @PathVariable @NotNull @Valid @Positive final Long eventId
    ) {
        return executor.execute(
                FetchGalleryEventUseCase.class,
                new FetchGalleryEventUseCase.Input(
                        photographerUserId,
                        eventId
                )
        );
    }


    @Operation(summary = "List the uploads made by the current logged in user", description =
        "This is an endpoint which simply wraps `GET /pub/list`. Look at its description "
        + "to understand how it works and how you should use it.")
    @GetMapping("/my-uploads")
    public @NotNull ListUploadsResponse listMyUploads(
            @RequestParam @Nullable @Valid @Positive final Long eventId,
            @RequestParam @Nullable @Valid @PositiveOrZero final Long fromUploadId,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                ListUploadsUseCase.class,
                new ListUploadsUseCase.Input(
                        fromUploadId,
                        user.getUserId(),
                        eventId,
                        user
                )
        );
    }


    @Operation(summary = "Permanently deletes the specified upload", description =
        "Keep in mind that only the media's photographer or a user with the "
        + "`UPLOADS_CAN_FULLY_DELETE_UPLOADS` permission can delete an upload. "
        + "For the latter, you can get the permissions of the user in the "
        + "`GET /users/display/me` request.")
    @DeleteMapping("/manage/{uploadId}")
    public boolean deleteUpload(
        @PathVariable @NotNull @Valid @Positive final Long uploadId,
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                DeleteUploadUseCase.class,
                new DeleteUploadUseCase.Input(
                        uploadId,
                        user
                )
        );
    }
}
