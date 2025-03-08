package net.furizon.backend.feature.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.LinkResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.usecase.GetPayOrderLink;
import net.furizon.backend.feature.room.dto.ExchangeAction;
import net.furizon.backend.feature.room.dto.ExchangeConfirmationStatus;
import net.furizon.backend.feature.room.dto.RoomInfo;
import net.furizon.backend.feature.room.dto.request.BuyUpgradeRoomRequest;
import net.furizon.backend.feature.room.dto.request.ChangeNameToRoomRequest;
import net.furizon.backend.feature.room.dto.request.CreateRoomRequest;
import net.furizon.backend.feature.room.dto.request.ExchangeRequest;
import net.furizon.backend.feature.room.dto.request.GuestIdRequest;
import net.furizon.backend.feature.room.dto.request.InviteToRoomRequest;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.dto.request.SetShowInNosecountRequest;
import net.furizon.backend.feature.room.dto.request.UpdateExchangeStatusRequest;
import net.furizon.backend.feature.room.dto.response.AdminSanityChecksResponse;
import net.furizon.backend.feature.room.dto.response.ExchangeConfirmationStatusResponse;
import net.furizon.backend.feature.room.dto.response.ListRoomPricesAvailabilityResponse;
import net.furizon.backend.feature.room.dto.response.RoomInfoResponse;
import net.furizon.backend.feature.room.usecase.BuyUpgradeRoomUseCase;
import net.furizon.backend.feature.room.usecase.CanConfirmRoomUseCase;
import net.furizon.backend.feature.room.usecase.CanUnconfirmRoomUseCase;
import net.furizon.backend.feature.room.usecase.ConfirmRoomUseCase;
import net.furizon.backend.feature.room.usecase.CreateRoomUseCase;
import net.furizon.backend.feature.room.usecase.DeleteRoomUseCase;
import net.furizon.backend.feature.room.usecase.ExchangeFullOrderUseCase;
import net.furizon.backend.feature.room.usecase.ExchangeRoomUseCase;
import net.furizon.backend.feature.room.usecase.GetExchangeConfirmationStatusInfoUseCase;
import net.furizon.backend.feature.room.usecase.GetRoomInfoUseCase;
import net.furizon.backend.feature.room.usecase.InitializeExchangeFlowUseCase;
import net.furizon.backend.feature.room.usecase.InviteAcceptUseCase;
import net.furizon.backend.feature.room.usecase.InviteCancelUseCase;
import net.furizon.backend.feature.room.usecase.InviteRefuseUseCase;
import net.furizon.backend.feature.room.usecase.InviteToRoomUseCase;
import net.furizon.backend.feature.room.usecase.KickMemberUseCase;
import net.furizon.backend.feature.room.usecase.LeaveRoomUseCase;
import net.furizon.backend.feature.room.usecase.ListRoomWithPricesAndQuotaUseCase;
import net.furizon.backend.feature.room.usecase.RenameRoomUseCase;
import net.furizon.backend.feature.room.usecase.SetShowInNosecountUseCase;
import net.furizon.backend.feature.room.usecase.UnconfirmRoomUseCase;
import net.furizon.backend.feature.room.usecase.UpdateExchangeStatusUseCase;
import net.furizon.backend.feature.user.dto.InviteToRoomResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.SanityCheckService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.annotation.PermissionRequired;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCaseExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Slf4j
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
    @org.jetbrains.annotations.NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;
    @org.jetbrains.annotations.NotNull
    private final GeneralChecks checks;

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
                        pretixInformation
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

    @Operation(summary = "Set if the room should be showed in the nosecount", description =
        "For privacy reasons, some people might prefer to hide their room in the nosecount. "
        + "If this is set to false, the users in this room will appear in the 'ticketless furs' section "
        + "of the nosecount, so they won't be displayed together nor it won't be showed in which kind "
        + "of room and in which hotel the people are staying. Only the owner of the room can set this "
        + "property. Administrator can specify to which room this option should be changed by using the "
        + "optional `roomId` parameter. If omitted, the user's room will be used instead. "
        + "We return whenever the function was successful or not")
    @PostMapping("/show-in-nosecount")
    public boolean showInNosecount(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final SetShowInNosecountRequest req
    ) {
        return executor.execute(
                SetShowInNosecountUseCase.class,
                new SetShowInNosecountUseCase.Input(
                        req,
                        user,
                        pretixInformation.getCurrentEvent()
                )
        );
    }

    @NotNull
    @Operation(summary = "Invites (forcefully, if desired) users to the room", description =
        "This operation can be performed only by the room's owner or an administrator. "
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
                        pretixInformation.getCurrentEvent(),
                        guestIdRequest
                )
        );
    }

    @Operation(summary = "Cancel the specified invitation", description =
            "This operation can be done only by someone who owns a room. "
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
        "Kicks the person specified in the `guestId` parameter from the room. "
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
            @Nullable @Valid @RequestBody final RoomIdRequest roomIdRequest
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
            @Nullable @Valid @RequestBody final RoomIdRequest roomIdRequest
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
            @Nullable @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                ConfirmRoomUseCase.class,
                new ConfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation
                )
        );
    }

    @Operation(summary = "Unconfirms the current room", description =
            "This operation can be performed only by the room's owner or by an admin. "
            + "By default, it unconfirms the room owned by the current user. If this operation "
            + "is performed by an admin, the room to confirm can be specified in the `roomId` param.")
    @PostMapping("/unconfirm")
    public boolean unconfirmRoom(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Nullable @Valid @RequestBody final RoomIdRequest roomIdRequest
    ) {
        return executor.execute(
                UnconfirmRoomUseCase.class,
                new UnconfirmRoomUseCase.Input(
                        user,
                        roomIdRequest,
                        pretixInformation
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
        boolean success = executor.execute(
                BuyUpgradeRoomUseCase.class,
                new BuyUpgradeRoomUseCase.Input(
                        user,
                        req,
                        pretixInformation
                )
        );
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
    @PermissionRequired(permissions = {Permission.CAN_MANAGE_ROOMS})
    @PostMapping("/run-sanity-checks")
    public AdminSanityChecksResponse runSanityChecks(
            @AuthenticationPrincipal @NotNull final FurizonUser user
    ) {
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
            new GetRoomInfoUseCase.Input(
                    user.getUserId(),
                    pretixInformation.getCurrentEvent(),
                    pretixInformation,
                    false
            )
        );
    }

    @Operation(summary = "Obtains information about the specified exchange", description =
        "Obtains the information about the specific exchange. Used to display the confirmation page. "
        + "It contains display information for both user, if the users have confirmed, the exchange action "
        + "and: If the exchange is a full order exchange, it contains the order data of the source user "
        + "which is going to be transferred to the target user. If the exchange is a room exchange, it "
        + "contains for sure the roomData of the source user, and, if the target user has already "
        + "confirmed or if the requester is the target user, the roomData of the target user.")
    @GetMapping("/exchange/info")
    public ExchangeConfirmationStatusResponse getExchangeConfirmationStatus(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @Valid @NotNull @RequestParam("id") Long exchangeId
    ) {
        return executor.execute(
                GetExchangeConfirmationStatusInfoUseCase.class,
                new GetExchangeConfirmationStatusInfoUseCase.Input(
                        user,
                        exchangeId,
                        pretixInformation,
                        false
                )
        );
    }

    @Operation(summary = "Starts a room or full order transfer/exchange", description =
        "This method, after verifying that all conditions for the exchange are met, "
        + "will start the exchange flow by sending emails to both parties to confirm "
        + "or refuse the exchange. Only after both have confirmed the actual transfer will happen. "
        + "With the `action` paramether you can choose to run a full order transfer or a "
        + "room transfer/exchange. If `sourceUserId` the current user is used, otherwise "
        + "the one specified. Admin checks are done over the userId to check if the logged "
        + "user can operate on it")
    @PostMapping("/exchange/init")
    public boolean initTransferExchange(
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
                            true
                    )
            );
            case ExchangeAction.TRASFER_FULL_ORDER -> executor.execute(
                    ExchangeFullOrderUseCase.class,
                    new ExchangeFullOrderUseCase.Input(
                            user,
                            req,
                            pretixInformation,
                            true
                    )
            );
        };
        if (success) {
            success = executor.execute(
                    InitializeExchangeFlowUseCase.class,
                    new InitializeExchangeFlowUseCase.Input(
                            user,
                            req,
                            pretixInformation.getCurrentEvent()
                    )
            );
        }
        return success;
    }

    @Operation(summary = "Updates the exchange status, and if both parties have confirmed, actually run the exchange",
        description = "It updates the exchange status. If `confirmed == false`, it deletes the exchange attempt, "
        + "so everyone is free to retry it. If both users have confirmed, it deletes the exchange attempt anyway, "
        + "BUT it will also run the actual exchange specified in the `action` parameter of the original /init request")
    @PostMapping("/exchange/update")
    public boolean updateExchangeStatus(
            @AuthenticationPrincipal @NotNull final FurizonUser user,
            @NotNull @Valid @RequestBody final UpdateExchangeStatusRequest req
    ) {
        long reqUserId = checks.getUserIdAndAssertPermission(req.getUserId(), user);
        ExchangeConfirmationStatus status = executor.execute(
                UpdateExchangeStatusUseCase.class,
                new UpdateExchangeStatusUseCase.Input(
                        reqUserId,
                        req
                )
        );
        boolean success = true;
        long userId = status.getSourceUserId();
        log.info("Exchange update status: src ({}) = {}; ({}) = dst {}",
                userId, status.isSourceConfirmed(), status.getTargetUserId(), status.isTargetConfirmed());
        if (status.isFullyConfirmed()) {
            ExchangeRequest exchangeRequest = new ExchangeRequest(
                    userId,
                status.getTargetUserId(),
                status.getAction()
            );
            var auth = Objects.requireNonNull(sessionAuthenticationManager.findAuthenticationByUserId(userId));
            FurizonUser fakeUser = FurizonUser.builder()
                    .userId(userId)
                    .authentication(auth)
                    .sessionId(user.getSessionId())
                    .build();

            log.info("Exchange is fully confirmed, running action {} with usr {}",
                    status.getAction(), fakeUser.getUserId());
            switch (status.getAction()) {
                case ExchangeAction.TRASFER_EXCHANGE_ROOM -> executor.execute(
                        ExchangeRoomUseCase.class,
                        new ExchangeRoomUseCase.Input(
                                fakeUser,
                                exchangeRequest,
                                pretixInformation,
                                false
                        )
                );
                case ExchangeAction.TRASFER_FULL_ORDER -> executor.execute(
                        ExchangeFullOrderUseCase.class,
                        new ExchangeFullOrderUseCase.Input(
                                fakeUser,
                                exchangeRequest,
                                pretixInformation,
                                false
                        )
                );
                default -> throw new IllegalStateException("Unexpected value: " + status.getAction());
            }
        }
        return success;
    }
}
