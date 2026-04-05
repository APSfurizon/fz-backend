package net.furizon.backend.feature.gallery.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadResponse;
import net.furizon.backend.feature.gallery.dto.request.AdminSetSelectedUploadRequest;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadRequest;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadResponse;
import net.furizon.backend.feature.gallery.dto.request.AdminUpdateUploadRequest;
import net.furizon.backend.feature.gallery.dto.response.AdminBatchApprovalResponse;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryEventsResponse;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryPhotographersResponse;
import net.furizon.backend.feature.gallery.dto.response.ListUploadsResponse;
import net.furizon.backend.feature.gallery.usecase.*;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;

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
        + "be display a default icon. You can freely filter over photographers and events, by "
        + "their id. An user with the permission `UPLOADS_CAN_MANAGE_UPLOADS` can also filter "
        + "by the status of the upload. If the param is specified, but the user doesn't have "
        + "the correct permission, the parameter is simply ignored. However, if an user has "
        + "that permission and no uploadStatus is defined, he will see all the results, in "
        + "any possible status.")
    @GetMapping("/pub/list")
    public @NotNull ListUploadsResponse listUploads(
            @RequestParam @Nullable @Valid @Positive final Long photographerUserId,
            @RequestParam @Nullable @Valid @Positive final Long eventId,
            @RequestParam @Nullable @Valid final UploadStatus uploadStatus,
            @RequestParam @Nullable @Valid @PositiveOrZero final Long fromUploadId,
            @AuthenticationPrincipal @Valid @Nullable final FurizonUser user
    ) {
        return executor.execute(
                ListUploadsUseCase.class,
                new ListUploadsUseCase.Input(
                        fromUploadId,
                        photographerUserId,
                        eventId,
                        uploadStatus,
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

    @Operation(summary = "List all the photographers with photos, with extra gallery-related information", description =
        "This endpoint should be used to display a list of photographers with their propic and photo number. "
        + "Like the `pub/events` endpoint, only photographers with actual photos are displayed. The field "
        + "`isOfficialPhotographer` is true if the photographer is recognized as an official account. "
        + "Frontend should display a small tag according to that, so users can understand which are the official "
        + "accounts. Photographer list is already ordered")
    @GetMapping("/pub/photographers")
    public @NotNull ListGalleryPhotographersResponse listPhotographers(
            @RequestParam @Nullable @Valid @Positive final Long eventId
    ) {
        return executor.execute(
                ListGalleryPhotographersUseCase.class,
                eventId == null ? -1L : eventId
        );
    }

    @Operation(summary = "Fetch the specified event, together with extra gallery-related information", description =
        "This endpoint behaves the same as `GET /pub/events`. Please look at his documentation. "
        + "The idea behind this single fetch is to have a big event card to show if a particular event is selected")
    @GetMapping("/pub/events/{eventId}")
    public @NotNull GalleryEvent fetchEvent(
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

    @Operation(summary = "Fetch the specified photographer, together with extra gallery-related information",
        description = "This endpoint behaves the same as `GET /pub/photographers`. Please look at his documentation.")
    @GetMapping("/pub/photographers/{photographerUserId}")
    public @NotNull GalleryPhotographer fetchPhotographer(
            @RequestParam @Nullable @Valid @Positive final Long eventId,
            @PathVariable @NotNull @Valid @Positive final Long photographerUserId
    ) {
        return executor.execute(
                FetchGalleryPhotogapherUseCase.class,
                new FetchGalleryPhotogapherUseCase.Input(
                        eventId,
                        photographerUserId
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
            @RequestParam @Nullable @Valid final UploadStatus uploadStatus,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                ListUploadsUseCase.class,
                new ListUploadsUseCase.Input(
                        fromUploadId,
                        user.getUserId(),
                        eventId,
                        uploadStatus,
                        user
                )
        );
    }

    @Operation(summary = "Request a bulk download of multiple uploads", description =
        "By sending via POST the array of upload ids an user wants to bulk download, this endpoint "
        + "will reply with an object containing a `url` and a `body`. The frontend will then "
        + "POST the specified url passing as request body the literal content returned by this endpoint. "
        + "The server will reply with the stream of a generated zip file containing all the requested uploads. "
        + "Keep in mind that the link returned by this endpoint eventually expires. Keep in mind that a single user "
        + "can have only one concurrent download in progress (it's verified by the final server). Only approved "
        + "uploads can be downloaded, invalid uploadIds or uploads in other status will be ignored")
    @PostMapping("/bulk-download")
    public @NotNull BulkDownloadResponse bulkDownloadInit(
            @NotNull @Valid @RequestBody final BulkDownloadRequest request,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                BulkGalleryDownloadUseCase.class,
                new BulkGalleryDownloadUseCase.Input(
                        new HashSet<>(request.getIds()),
                        user
                )
        );
    }

    @Operation(summary = "Request a bulk DIRECT download of multiple uploads", description =
        "This method works is similar to the bulk-download one, but instead of relying on an "
        + "external service to download a single zip file containing all the photos already "
        + "sorted in directories by event and photographer, it simply returns a direct download "
        + "link for each single selected media together with some extra information:"
        + "Inside every object we have the field"
        + "`u` containing the direct download Url, "
        + "`n` containing the original file name (keep in mind that files on server are saved with uuids, "
        + "you have to manually fix the filename), "
        + "`t` an upload timestamp, if needed for any reason, "
        + "`s` the size of the file. "
        + "This method is intended to be used on, for example, mobile platforms where it's better to download "
        + "the direct medias instead of having a single huge zip file on disk. The link you get with this "
        + "endpoint have NO expiration.")
    @PostMapping("/bulk-direct-download")
    public @NotNull BulkDirectDownloadResponse bulkDirectDownloadInit(
            @NotNull @Valid @RequestBody final BulkDownloadRequest request,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
            BulkGalleryDirectDownloadUseCase.class,
                new BulkGalleryDirectDownloadUseCase.Input(
                    new HashSet<>(request.getIds()),
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

    @Operation(summary = "Request a new approval batch of pending medias", description =
        "This method is intended for admin use only. It returns a batch of medias who still "
        + "need to be approved, so an admin can approve or reject them (or change event and photographer). "
        + "To not let different admins compete between each other, once you receive a batch you have a lock "
        + "on it for a couple of minutes. Other admins will not receive the same photos as yours during this time. "
        + "Each time you request a new batch, you left behind any unapproved or unrejected photo so far and continue "
        + "up in the list. If you want to restart from the first pending upload you can pass the parameter "
        + "`firstRequest` set on true (if you don't specify it, by default it's assumed false). Per each batch, "
        + "uploads belongs only to one single uploader on one single event. Orders of uploads is not always "
        + "respected. Receiving an empty upload list means that the upload process it's done *for now*, of course "
        + "users can still upload new medias. The results are the same as you can find them in the "
        + "`GET `/pub/list`, so read its docs to understand the single object. This method returns "
        + "the event card as well, the photographer display data and how many uploads are still pending.")
    @PermissionRequired(permissions = {Permission.UPLOADS_CAN_MANAGE_UPLOADS})
    @GetMapping("/manage/approval-batch")
    public AdminBatchApprovalResponse getApprovalBatch(
        @RequestParam @Nullable @Valid final Boolean firstRequest,
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                AdminBatchListUseCase.class,
                new AdminBatchListUseCase.Input(
                        firstRequest == null ? false : firstRequest,
                        user
                )
        );
    }

    @Operation(summary = "Updates status, eventId, photographerId to a set of uploads", description =
        "This method is for admin use only, the user must have the `UPLOADS_CAN_MANAGE_UPLOADS` permission "
        + "to perform this operation. We don't permit on purpose users changing any information on an uploaded "
        + "media (like the eventId), only an administrator can perform this. The operation of changing information "
        + "on the various uploads can be done in bulk, just specify more ids in the list you want to change. "
        + "This endpoint actually verifies that new photographer and event exists. It also checks that you "
        + "specify at least one of the parameter you want to change (this implies that you must specify "
        + "only the info you really want to update).")
    @PermissionRequired(permissions = {Permission.UPLOADS_CAN_MANAGE_UPLOADS})
    @PostMapping("/manage/update")
    public boolean updateUploads(
        @NotNull @Valid @RequestBody final AdminUpdateUploadRequest request,
        @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                AdminUpdateUploadUseCase.class,
                new AdminUpdateUploadUseCase.Input(
                        request.getUploadIds(),
                        request.getNewStatus(),
                        request.getNewPhotographerUserId(),
                        request.getNewEventId(),
                        user
                )
        );
    }

    @Operation(summary = "Updates the selection status of an upload", description =
        "Keep in mind that only images can be set as selected. Only one selection "
        + "is allowed per event, by selecting a photo all the others from the same "
        + "event will be automatically deselected. The event is automatically inferred "
        + "from the specified upload id")
    @PermissionRequired(permissions = {Permission.UPLOADS_CAN_MANAGE_UPLOADS})
    @PostMapping("/manage/set-selected")
    public GalleryUpload setSelectedToUpload(
            @NotNull @Valid @RequestBody final AdminSetSelectedUploadRequest request,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                AdminSetSelectedUploadUseCase.class,
                new AdminSetSelectedUploadUseCase.Input(
                        request.getUploadId(),
                        request.getSelected(),
                        user
                )
        );
    }
}
