package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

import static net.furizon.backend.infrastructure.email.EmailVars.LINK;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_TYPE_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.LANG_PRETIX;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_UNCONFIRMED;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnconfirmRoomUseCase implements UseCase<UnconfirmRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final FrontendConfig frontendConfig;

    @Override
    public @NotNull Boolean executor(@NotNull UnconfirmRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long roomId = checks.getRoomIdAssertPermissionCheckTimeframe(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomConfirmed(roomId);
        //Following check should be done inside the confirmation method for performance reasons
        //checks.assertRoomCanBeUnconfirmed(roomId, event, input.pretixInformation, roomLogic);
        checks.assertRoomFromCurrentEvent(roomId, event);

        Long roomItemId = roomFinder.getRoomItemIdFromRoomId(roomId);
        boolean res = roomLogic.unconfirmRoom(roomId, event, input.pretixInformation);
        if (res) {
            if (roomItemId != null) {
                Map<String, String> names = pretixInformation.getRoomNamesFromRoomPretixItemId(roomItemId);
                if (names != null && !names.isEmpty()) {
                    String name = names.get(LANG_PRETIX);
                    if (name != null) {
                        mailService.prepareAndSendBroadcastUpdate(
                            roomId,
                            TEMPLATE_ROOM_UNCONFIRMED,
                            TranslatableValue.ofEmail("mail.room_unconfirmed.title"),
                            MailVarPair.of(ROOM_TYPE_NAME, name),
                            MailVarPair.of(LINK, frontendConfig.getRoomPageUrl())
                        );
                    }
                }
            }
        }
        return res;
    }

    public record Input(
            @NotNull FurizonUser user,
            @Nullable RoomIdRequest roomReq,
            @NotNull PretixInformation pretixInformation
    ) {}
}
