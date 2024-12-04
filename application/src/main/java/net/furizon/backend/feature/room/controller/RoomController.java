package net.furizon.backend.feature.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.request.ChangeNameToRoomRequest;
import net.furizon.backend.feature.room.dto.request.CreateRoomRequest;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.room.usecase.userBuysFullRoom.GetRoomInfoUseCase;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;
    @org.jetbrains.annotations.NotNull
    private final RoomLogic roomLogic;
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;

    @NotNull
    @Operation(summary = "Creates a new room", description =
        "If the user doesn't have created a new room yet, it creates a new room with the specified name")
    @PostMapping("/create")
    public RoomInfo createRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final CreateRoomRequest createRoomRequest
    ) {
        return null;
    }

    @Operation(summary = "Deletes user's room", description =
        "By default, it deletes the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `roomId` parameter, he can choose to delete the room of another user")
    @PostMapping("/delete")
    public boolean deleteRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Renames user's room", description =
        "By default, it renames the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `roomId` parameter, he can choose to rename the room of another user")
    @PostMapping("/change-name")
    public boolean changeRoomName(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final ChangeNameToRoomRequest changeNameToRoomRequest
    ) {
        return false;
    }

    @NotNull
    @Operation(summary = "Invites (also forcefully) another user to the room", description =
        "This operation can be performed only by the room's owner or an administrator ."
        + "It invites the person specified in the `userId` param in the owner's room. "
        + "An administrator  needs to specify the `roomId` param as well. "
        + "By setting `force` to true, this operation will bypass the invitation step. "
        + "If the user is already part of a room and the `forceExit` param is set to `true`, then the user "
        + "it's forcefully moved to this room, otherwise an error is returned")
    @PostMapping("/invite")
    public RoomGuestResponse invitePersonToRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final InviteToRoomRequest inviteToRoomRequest
    ) {
        return null;
    }

    @Operation(summary = "Accept the specified invitation", description =
        "You can invite only people who don't are part of a room yet. "
        + "Using the `guestId` param you can specify which invitation to accept")
    @PostMapping("/invite/accept")
    public boolean acceptInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Refuse the specified invitation", description =
        "This operation can be done only by someone who doesn't own a room. "
        + "Using the `guestId` param you can specify which invitation to refuse")
    @PostMapping("/invite/refuse")
    public boolean refuseInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Cancel the specified invitation", description =
            "This operation can be done only by someone who doesn't own a room. "
            + "Using the `guestId` param you can specify which invitation to cancel")
    @PostMapping("/invite/cancel")
    public boolean cancelInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Kicks someone from the room", description =
        "Kicks the person specified in the `guestId` parameter from the room"
        + "This operation can be performed only by the room's owner or by an administrator.")
    @PostMapping("/kick")
    public boolean kickFromRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Leaves the current room", description =
        "This operation can be performed only by a guest in a room.")
    @PostMapping("/leave")
    public boolean leaveRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Confirms the current room", description =
        "This operation can be performed only by the room's owner or by an admin. "
        + "After the room has be confirmed, it cannot change the guests nor be deleted. "
        + "By default, it confirms the room owned by the current user. If this operation "
        + "is performed by an admin, the room to confirm can be specified in the `roomId` param.")
    @PostMapping("/confirm")
    public boolean confirmRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Unconfirms the current room", description =
            "This operation can be performed only by the room's owner or by an admin."
            + "By default, it unconfirms the room owned by the current user. If this operation "
            + "is performed by an admin, the room to confirm can be specified in the `roomId` param.")
    @PostMapping("/unconfirm")
    public boolean unconfirmRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return false;
    }

    @Operation(summary = "Gets the current user's room status and information", description =
            "This method returns various information about the status of the accomodance of the user")
    @GetMapping("/info")
    @NotNull
    public RoomInfoResponse getRoomInfo(@AuthenticationPrincipal @NotNull final FurizonUser user) {
        return executor.execute(
            GetRoomInfoUseCase.class,
            new GetRoomInfoUseCase.Input(user, getEvent())
        );
    }

    @Nullable
    private Event getEvent() {
        return pretixInformation.getCurrentEvent().orElse(null);
    }
}
