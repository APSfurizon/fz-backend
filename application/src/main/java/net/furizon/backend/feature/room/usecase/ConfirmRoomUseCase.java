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
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_TYPE_NAME;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.LANG_PRETIX;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_ROOM_CONFIRMED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmRoomUseCase implements UseCase<ConfirmRoomUseCase.Input, Boolean> {
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomChecks roomChecks;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final MailRoomService mailService;

    @Override
    public @NotNull Boolean executor(@NotNull ConfirmRoomUseCase.Input input) {
        long requesterUserId = input.user.getUserId();
        log.info("User {} is confirming a room", requesterUserId);
        PretixInformation pretixInformation = input.pretixInformation;
        Event event = pretixInformation.getCurrentEvent();

        long roomId = roomChecks.getRoomIdAndAssertPermissionsOnRoom(
                requesterUserId,
                event,
                input.roomReq == null ? null : input.roomReq.getRoomId()
        );
        roomChecks.assertRoomNotConfirmed(roomId);
        roomChecks.assertRoomCanBeConfirmed(roomId, event, roomLogic);
        roomFinder.getRoomGuestsFromRoomId(roomId, true).forEach(g -> generalChecks.assertOrderIsPaid(g.getUserId(), event));

        List<String> errors = new LinkedList<>();
        roomLogic.doSanityChecks(roomId, pretixInformation, errors);
        if (errors.size() > 0) {
            log.error("Sanity checks failed while confirming room {}", roomId);
            return false;
        }

        boolean res = roomLogic.confirmRoom(roomId);
        if (res) {
            Long roomItemId = roomFinder.getRoomItemIdFromRoomId(roomId);
            if (roomItemId != null) {
                Map<String, String> names = pretixInformation.getRoomNamesFromRoomPretixItemId(roomItemId);
                if (names != null && !names.isEmpty()) {
                    mailService.broadcastUpdate(
                            roomId, TEMPLATE_ROOM_CONFIRMED, MailVarPair.of(ROOM_TYPE_NAME, names.get(LANG_PRETIX))
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
