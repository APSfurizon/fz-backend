package net.furizon.backend.feature.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {
    @NotNull
    private final PretixInformation pretixInformation;
    @NotNull private final UseCaseExecutor executor;

    @Operation(summary = "Creates a new room", description =
        "If the user doesn't have created a new room yet, it creates a new room with the specified name")
    @PostMapping("/create")
    public void createRoom(){

    }

    @Operation(summary = "Deletes user's room", description =
        "By default, it deletes the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `id` parameter, he can choose to delete the room of another user")
    @PostMapping("/delete")
    public void deleteRoom(){

    }

    @Operation(summary = "Renames user's room", description =
        "By default, it renames the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `id` parameter, he can choose to rename the room of another user")
    @PostMapping("/change-name")
    public void changeRoomName(){

    }

    @Operation(summary = "Invites another user to the room", description =
        "This operation can be performed only by the room's owner.")
    @PostMapping("/invite")
    public void invitePersonToRoom(){ //TODO search by what?

    }

    @Operation(summary = "Forcefully let an user inside a room", description =
            "This operation can be performed only by an administrator. "
            + "This operation forcefully inserts the specified user inside the room, bypassing the invitation step. "
            + "If the user is already part of a room and the `forceExit` param is set to `true`, then the user "
            + "it's forcefully moved to this room, otherwise an error is returned")
    @PostMapping("/invite/force")
    public void forcefullyInvitePersonToRoom(){ //TODO search by what?

    }

    @Operation(summary = "Accept the specified invitation", description =
        "You can invite only people who don't are part of a room yet. "
        + "Using the `guestId` param you can specify which invitation to accept")
    @PostMapping("/invite/accept")
    public void acceptInvitation(){

    }

    @Operation(summary = "Refuse the specified invitation", description =
        "This operation can be done only by someone who doesn't own a room. "
        + "Using the `guestId` param you can specify which invitation to refuse")
    @PostMapping("/invite/refuse")
    public void refuseInvitation(){

    }

    @Operation(summary = "Cancel the specified invitation", description =
            "This operation can be done only by someone who doesn't own a room. "
            + "Using the `guestId` param you can specify which invitation to cancel")
    @PostMapping("/invite/cancel")
    public void cancelInvitation(){

    }

    @Operation(summary = "Kicks someone from the room", description =
        "Kicks the person specified in the `guestId` parameter from the room"
        + "This operation can be performed only by the room's owner or by an administrator.")
    @PostMapping("/kick")
    public void kickFromRoom(){

    }

    @Operation(summary = "Leaves the current room", description =
        "This operation can be performed only by a guest in a room.")
    @PostMapping("/leave")
    public void leaveRoom(){

    }

    @Operation(summary = "Confirms the current room", description =
        "This operation can be performed only by the room's owner or by an admin. "
        + "After the room has be confirmed, it cannot change the guests nor be deleted. "
        + "By default, it confirms the room owned by the current user. If this operation "
        + "is performed by an admin, the room to confirm can be specified in the `id` param.")
    @PostMapping("/confirm")
    public void confirmRoom(){

    }

    @Operation(summary = "Unconfirms the current room", description =
            "This operation can be performed only by the room's owner or by an admin."
            + "By default, it unconfirms the room owned by the current user. If this operation "
            + "is performed by an admin, the room to confirm can be specified in the `id` param.")
    @PostMapping("/unconfirm")
    public void unconfirmRoom(){

    }

    @Operation(summary = "Gets the current user's room status and information", description =
            "This method returns various information about the status of the accomodance of the user")
    @GetMapping("/info")
    public void getRoomInfo(){

    }
}
