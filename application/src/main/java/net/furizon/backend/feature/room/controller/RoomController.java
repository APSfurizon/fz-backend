package net.furizon.backend.feature.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.GetPayOrderLink;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.request.*;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.dto.response.AdminSanityChecksResponse;
import net.furizon.backend.feature.room.dto.response.ListRoomPricesAvailabilityResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.usecase.*;
import net.furizon.backend.feature.user.dto.InviteToRoomResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.SanityCheckService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {
    @org.jetbrains.annotations.NotNull
    private final PretixInformation pretixInformation;
    @org.jetbrains.annotations.NotNull
    private final UseCaseExecutor executor;
    @org.jetbrains.annotations.NotNull
    private final SanityCheckService sanityCheckService;

    @NotNull
    @Operation(summary = "Creates a new room", description =
        "If the user hasn't created a new room yet, it creates a new room with the specified name")
    @PostMapping("/create")
    public RoomInfo createRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final CreateRoomRequest createRoomRequest
    ) {
        return executor.execute(
                CreateRoomUseCase.class,
                new CreateRoomUseCase.Input(
                    user,
                    createRoomRequest,
                    pretixInformation.getCurrentEvent(),
                    pretixInformation
                )
        );
    }

    @Operation(summary = "Deletes user's room", description =
        "By default, it deletes the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `roomId` parameter, he can choose to delete the room of another user")
    @PostMapping("/delete")
    public boolean deleteRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                DeleteRoomUseCase.class,
                new DeleteRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Renames user's room", description =
        "By default, it renames the user's room. This operation can be performed only by the room's owner. "
        + "If the user is an admin, using the `roomId` parameter, he can choose to rename the room of another user")
    @PostMapping("/change-name")
    public boolean changeRoomName(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final ChangeNameToRoomRequest changeNameToRoomRequest
    ) {
        return executor.execute(
                RenameRoomUseCase.class,
                new RenameRoomUseCase.Input(
                        user,
                        changeNameToRoomRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @NotNull
    @Operation(summary = "Invites (forcefully, if desired) users to the room", description =
        "This operation can be performed only by the room's owner or an administrator ."
        + "It invites the users specified in the `userId` param in the owner's room. "
        + "An administrator needs to specify the `roomId` param as well. "
        + "By setting `force` to true, this operation will bypass the invitation step. "
        + "If the users are already part of a room and the `forceExit` param is set to `true`, "
        + "then they're forcefully moved to this room, otherwise an error is returned")
    @PostMapping("/invite")
    public InviteToRoomResponse invitePersonToRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final InviteToRoomRequest inviteToRoomRequest
    ) {
        return executor.execute(
                InviteToRoomUseCase.class,
                new InviteToRoomUseCase.Input(
                        user,
                        inviteToRoomRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Accept the specified invitation", description =
        "This operation can be done only by someone who doesn't own a room. "
        + "Using the `guestId` param you can specify which invitation to accept")
    @PostMapping("/invite/accept")
    public boolean acceptInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return executor.execute(
                InviteAcceptUseCase.class,
                new InviteAcceptUseCase.Input(
                        user,
                        guestIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Refuse the specified invitation", description =
        "Using the `guestId` param you can specify which invitation to refuse")
    @PostMapping("/invite/refuse")
    public boolean refuseInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return executor.execute(
                InviteRefuseUseCase.class,
                new InviteRefuseUseCase.Input(
                        user,
                        guestIdRequest
                )
        );
    }

    @Operation(summary = "Cancel the specified invitation", description =
            "This operation can be done only by someone who doesn't own a room. "
            + "Using the `guestId` param you can specify which invitation to cancel")
    @PostMapping("/invite/cancel")
    public boolean cancelInvitation(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return executor.execute(
                InviteCancelUseCase.class,
                new InviteCancelUseCase.Input(
                        user,
                        guestIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Kicks someone from the room", description =
        "Kicks the person specified in the `guestId` parameter from the room"
        + "This operation can be performed only by the room's owner or by an administrator.")
    @PostMapping("/kick")
    public boolean kickFromRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final GuestIdRequest guestIdRequest
    ) {
        return executor.execute(
                KickMemberUseCase.class,
                new KickMemberUseCase.Input(
                        user,
                        guestIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Leaves the current room", description =
        "This operation can be performed only by a guest in a room.")
    @PostMapping("/leave")
    public boolean leaveRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                LeaveRoomUseCase.class,
                new LeaveRoomUseCase.Input(
                        user,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Checks if the user can confirm the room", description =
        "The confirmation of the room is highly dependent on the room's chosen logic. "
        + "Some logic may accept confirmations with any number of users in the room, "
        + "others only if the room is full and others may not support confirmation at all! "
        + "This method returns whenever or not a confirmation button *should be showed* to the user. "
        + "Since, as previously said, some logic may never allow room confirmation, the button must be "
        + "hidden to the user and NOT disabled, if he can't confirm a room.")
    @GetMapping("/can-confirm")
    public boolean canConfirm(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Null @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                CanConfirmRoomUseCase.class,
                new CanConfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Checks if the user can unconfirm the room", description =
        "The unconfirmation of the room is highly dependent on the room's chosen logic. "
        + "Some logic may accept unconfirmations, while others won't. "
        + "This method returns whenever or not a confirmation button *should be showed* to the user. "
        + "Since, as previously said, some logic may never allow room unconfirmation, the button must be "
        + "hidden to the user and NOT disabled, if he can't confirm a room.")
    @GetMapping("/can-unconfirm")
    public boolean canUnconfirm(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Null @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                CanUnconfirmRoomUseCase.class,
                new CanUnconfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Confirms the current room", description =
        "This operation can be performed only by the room's owner or by an admin. "
        + "After the room has be confirmed, it cannot change the guests nor be deleted. "
        + "By default, it confirms the room owned by the current user. If this operation "
        + "is performed by an admin, the room to confirm can be specified in the `roomId` param.")
    @PostMapping("/confirm")
    public boolean confirmRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Null @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                ConfirmRoomUseCase.class,
                new ConfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Unconfirms the current room", description =
            "This operation can be performed only by the room's owner or by an admin."
            + "By default, it unconfirms the room owned by the current user. If this operation "
            + "is performed by an admin, the room to confirm can be specified in the `roomId` param.")
    @PostMapping("/unconfirm")
    public boolean unconfirmRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Null @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                UnconfirmRoomUseCase.class,
                new UnconfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @Operation(summary = "Gets a list of room with prices and quota", description =
        "This method is intended to be used in the buy or upgrade room flow "
        + "to display a list of rooms available to the user, together with their "
        + "prices and quota. Rooms are automatically filtered to what an user can "
        + "buy with their already purchased room. If no element is returned, a text like "
        + "\"there's no room you can currently buy\" should be displayed. Together with the "
        + "room list, the current room names and current room prices is returned to be displayed. "
        + "Using the returned price, you can also calc and show the difference between what the "
        + "user has paid and how much the room costs. Note that this difference may not be accurate")
    @GetMapping("/get-room-list-with-quota")
    public ListRoomPricesAvailabilityResponse getRoomList(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        return executor.execute(
                ListRoomWithPricesAndQuotaUseCase.class,
                new ListRoomWithPricesAndQuotaUseCase.Input(
                        user,
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Buys or upgrades a room", description =
        "This method takes in input a pretix itemId of a room and tries to book it into the "
        + "already existent user reservation. Admins can specify a userId of another user to "
        + "buy the room for them. The room's price must be equal or higher than the room that "
        + "the specified user currently owns. The returned value is empty if an error occurred, "
        + "otherwise it's a link where the frontend should redirect the user to complete the payment.")
    @PostMapping("/buy-or-upgrade-room")
    public LinkResponse buyOrUpgradeRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final BuyUpgradeRoomRequest req
    ) {
        boolean success = true; //TODO
        return success ? executor.execute(GetPayOrderLink.class,
                new GetPayOrderLink.Input(
                        user,
                        pretixInformation
                )
        ) : new LinkResponse("");
    }

    @Operation(summary = "Run immediately the room sanity checks", description =
        "This method is intended to be used by admin only. Runs immediately the room "
        + "sanity checks to detect if there's something wrong with the rooms. "
        + "This method is sync, expect a long running time! The returned array contains "
        + "a list of error logs detailing all the incongruences found. It should directly "
        + "be displayed to the admin (be aware of xss tho)")
    @PostMapping("/run-sanity-checks")
    public AdminSanityChecksResponse runSanityChecks(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
        //TODO [ADMIN_CHECK]
        List<String> errors = sanityCheckService.runSanityChecks();
        return new AdminSanityChecksResponse(errors);
    }

    @Operation(summary = "Gets the current user's room status and information", description =
            "This method returns various information about the status of the accommodation of the user."
            + "Please read the other documentation of this controller to better understand the various fields")
    @GetMapping("/info")
    @NotNull
    public RoomInfoResponse getRoomInfo(@AuthenticationPrincipal @NotNull final FurizonUser user) {
        return executor.execute(
            GetRoomInfoUseCase.class,
            new GetRoomInfoUseCase.Input(user, pretixInformation.getCurrentEvent(), pretixInformation)
        );
    }

    @Operation(summary = "Starts a room or full order transfer/exchange", description =
        "This method, after verifying that all conditions for the exchange are met,"
        + "will start the exchange flow by sending emails to both parties to confirm "
        + "or refuse the exchange. Only after both have confirmed the actual transfer will happen")
    @PostMapping("/init-exchange")
    public void initTransferExchange(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final ExchangeRequest req
    ) {
        boolean success = switch (req.getAction()) {
            case ExchangeAction.TRASFER_EXCHANGE_ROOM -> executor.execute(
                    ExchangeRoomUseCase.class,
                    new ExchangeRoomUseCase.Input(
                            user,
                            req,
                            pretixInformation,
                            null,
                            true
                    )
            );
            case ExchangeAction.TRASFER_FULL_ORDER -> executor.execute(
                    ExchangeFullOrderUseCase.class,
                    new ExchangeFullOrderUseCase.Input(
                            user,
                            req,
                            pretixInformation,
                            null,
                            true
                    )
            );
        };
        if (success) {
            executor.execute(
                    InitializeExchangeFlowUseCase.class,
                    new InitializeExchangeFlowUseCase.Input(
                            user,
                            req,
                            pretixInformation.getCurrentEvent()
                    )
            );
        }
    }
}
