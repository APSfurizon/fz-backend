package net.furizon.backend.feature.room.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.room.dto.request.RoomIdRequest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_TYPE_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.LANG_PRETIX;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteRoomUseCase implements UseCase<DeleteRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        long requesterUserId = input.user.getUserId();
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        checks.assertInTimeframeToEditRooms();
        long roomId = checks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        checks.assertRoomNotConfirmed(roomId);

        boolean res = roomLogic.deleteRoom(roomId);
        if (res) {
            Long roomItemId = roomFinder.getRoomItemIdFromRoomId(roomId);
            if (roomItemId != null) {
                Map<String, String> names = pretixInformation.getRoomNamesFromRoomPretixItemId(roomItemId);
                if (names != null) {
                    mailService.broadcastUpdate(
                            roomId, TEMPLATE_ROOM_DELETED, MailVarPair.of(ROOM_TYPE_NAME, names.get(LANG_PRETIX))
                    );
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
