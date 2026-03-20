package net.furizon.backend.feature.gallery.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.request.CompleteUploadRequest;
import net.furizon.backend.feature.gallery.dto.request.S3UploadRequest;
import net.furizon.backend.feature.gallery.dto.request.StartUploadRequest;
import net.furizon.backend.feature.gallery.dto.response.ListUploadPartsResponse;
import net.furizon.backend.feature.gallery.dto.response.StartUploadResponse;
import net.furizon.backend.feature.gallery.dto.response.UploadLimitsResponse;
import net.furizon.backend.feature.gallery.usecase.GetUploadLimitsUseCase;
import net.furizon.backend.feature.gallery.usecase.uploadProgress.AbortUploadUseCase;
import net.furizon.backend.feature.gallery.usecase.uploadProgress.CompleteUploadUseCase;
import net.furizon.backend.feature.gallery.usecase.uploadProgress.ListUploadUseCase;
import net.furizon.backend.feature.gallery.usecase.uploadProgress.StartUploadUseCase;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gallery/upload")
@RequiredArgsConstructor
public class GalleryUploadController {
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @Operation(summary = "Get upload limits of the user", description =
        "By default this method takes the current logged in user. An admin "
        + "with the `UPLOADS_CAN_MANAGE_UPLOADS` permission can specify "
        + "an optional userId to get info regarding him. Keep in mind "
        + "that if an admin is planning to upload a file in behalf of another user, "
        + "the limits might be a bit different, so don't rely on this endpoint for that "
        + "usecase. This endpoint returns: a list of event together with the number of uploads "
        + "the user has done on that specific event; a boolean to show if the user "
        + "is banned from making new uploads; the maximum number of medias an user can upload "
        + "per each single event (if == null it means unlimited uploads); the maximum file size "
        + "an user can upload (if == null, it means unlimited file size); a list of allowed mime types; "
        + "a list of allowed file extensions")
    @GetMapping("/limits")
    public @NotNull UploadLimitsResponse getUploadLimits(
            @RequestParam @Nullable @Valid @Positive final Long userId,
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user
    ) {
        return executor.execute(
                GetUploadLimitsUseCase.class,
                new GetUploadLimitsUseCase.Input(
                        userId,
                        user
                )
        );
    }

    @Operation(summary = "Starts an upload process", description =
        "The upload process will go through a multipart upload with presigned "
        + "links for each single part. Reference: "
        + "https://aws.amazon.com/it/blogs/compute/uploading-large-objects-to-amazon-s3-using-multipart-upload-and-transfer-acceleration/"
        + "You need to provide us the original file name which must match the regex "
        + "`^[\\p{L}\\p{N}\\p{M}_\\-\"'()\\[\\]. ]{2,63}$`, the file size and the event id "
        + "the user is planning to upload the event to. We will check now if the user can "
        + "actually upload the file to the event (IE uploads open for the said event, "
        + "size of the file is ok, user has not reach the upload quantity limit). "
        + "We will reply with an ORDERED list of presigned urls (where its index in the array "
        + "corresponds to the 1-based partnumber of them), together with the chunk size for all of them, "
        + "an id for this upload request, the s3 key of the file, the s3 endpoint, s3 bucket, "
        + "and a expiration timestamp after which you won't be able to "
        + "use the presigned urls anymore and the upload will be automatically aborted. Keep in mind that "
        + "**AN USER CAN UPLOAD ONLY ONE FILE PER TIME**, starting a new request will automatically "
        + "abort the previous one. This means that uploading different files cannot be parallelized, "
        + "however you can parallelize the upload of different chunks. Your application should have "
        + "retry and resume mechanisms related to the various chunks. Each single chunk MUST respect "
        + "the size specified in `chunkSize`, otherwise the upload will be rejected by s3. The last chunk "
        + "can be less the size of chunkSize, no padding is needed. If you have any error, you can call the "
        + "`/upload/abort` endpoint. Instead, once the uploading of all chunks is successful, call "
        + "`/upload/complete`. Check its docs for understanding its parameters. To upload each single chunk, "
        + "just do a PUT request towards the presigned url.")
    @PostMapping("/")
    public @NotNull StartUploadResponse startUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final StartUploadRequest req
    ) {
        return executor.execute(
                StartUploadUseCase.class,
                new StartUploadUseCase.Input(
                        req,
                        user
                )
        );
    }

    @Operation(summary = "Aborts an upload process", description =
        "To abort the process you need to provide the uploadReqId which is returned "
        + "from the `POST /upload` endpoint. For explicit failures on cancelation "
        + "it's better that you call this endpoint directly, but keep in mind anyway "
        + "that the upload will be aborted automatically when the expiration timestamp "
        + "(still returned from the start upload request) is reached.")
    @PostMapping("/abort")
    public boolean abortUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final S3UploadRequest req
    ) {
        return executor.execute(
                AbortUploadUseCase.class,
                new AbortUploadUseCase.Input(
                        req,
                        user
                )
        );
    }

    @Operation(summary = "Completes the upload process", description =
        "While you upload the various parts to s3, you have to md5 the single chunks (more on that later). "
        + "Moreover, after each upload s3 will reply with a string etag. You have to store them together "
        + "with the part number it's coming from and send them here in an ordered list, in the "
        + "`etags` field. Since you already know the number of chunks (it's signedUrls.length()), I suggest "
        + "pre allocating an array of strings of that size, and then for each response do a "
        + "`arr[partNo - 1] = eTag`. In this way you save yourself from a lot of problems, like concurrent access "
        + "to the list. Together with the etag list and the hex encoded md5, you have to send again the eventId "
        + "you're uploading the media to, the filename and the permission the user is giving to the repost of the "
        + "media. If everything goes correctly and the hash is correct, the object of the media is returned. Some "
        + "fields will still be missing since some processing is done async. If, while uploading the parts, you lose "
        + "the context and you need to understand again which parts you've uploaded so far, "
        + "use the `/upload/status` endpoint. Regarding the md5 hash: On each chunk, you have to compute a md5 "
        + "of it and store his raw-bytes digest. After you're done with uploading the various chunks you have to "
        + "append the various digests (in the same part order, I suggest an array solution like the etags) together "
        + "and compute an extra md5 of all of the appended digest. The hexdigest of dist must be sent in the "
        + "`md5hash` field of the request. I can understand that this process is not trivial, so I "
        + "suggest to take a look at the `uploadFileToGallery` function of the `testBackend.py` file you can find "
        + "in this repository.")
    @PostMapping("/complete")
    public GalleryUpload completeUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final CompleteUploadRequest req
    ) {
        return executor.execute(
                CompleteUploadUseCase.class,
                new CompleteUploadUseCase.Input(
                        req,
                        user
                )
        );
    }

    @Operation(summary = "Get the list of parts uploaded so far", description =
        "Reminder that aws uses 1-based indexing for multipart upload part number")
    @GetMapping("/status")
    public ListUploadPartsResponse statusUpload(
            @AuthenticationPrincipal @Valid @NotNull final FurizonUser user,
            @Valid @NotNull @RequestBody final S3UploadRequest req
    ) {
        return executor.execute(
                ListUploadUseCase.class,
                new ListUploadUseCase.Input(
                        req,
                        user
                )
        );
    }
}
