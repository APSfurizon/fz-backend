package net.furizon.backend.infrastructure.rooms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailRoomService {
    @NotNull private final EmailSender emailSender;
    @NotNull private final RoomFinder roomFinder;

    public void sendProblem(long userId, @NotNull String templateName, MailVarPair... vars) {
        emailSender.send(userId, RoomEmailTexts.SUBJECT_ROOM_PROBLEM, templateName, vars);
    }
    public void sendProblem(@NotNull UserEmailData data, @NotNull String templateName, MailVarPair... vars) {
        emailSender.send(data, RoomEmailTexts.SUBJECT_ROOM_PROBLEM, templateName, vars);
    }

    public void sendUpdate(long userId, @NotNull String templateName, MailVarPair... vars) {
        emailSender.send(userId, RoomEmailTexts.SUBJECT_ROOM_UPDATE, templateName, vars);
    }
    public void sendUpdate(@NotNull UserEmailData data, @NotNull String templateName, MailVarPair... vars) {
        emailSender.send(data, RoomEmailTexts.SUBJECT_ROOM_UPDATE, templateName, vars);
    }

    public void broadcastProblem(long roomId, @NotNull String templateName, MailVarPair... vars) {
        log.info("Sending broadcast problem to room: {}", roomId);
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        for (RoomGuest guest : guests) {
            emailSender.send(guest.getUserId(), RoomEmailTexts.SUBJECT_ROOM_PROBLEM, templateName, vars);
        }
    }

    public void broadcastUpdate(long roomId, @NotNull String templateName, MailVarPair... vars) {
        log.info("Sending broadcast update to room: {}", roomId);
        List<RoomGuest> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true);
        for (RoomGuest guest : guests) {
            emailSender.send(guest.getUserId(), RoomEmailTexts.SUBJECT_ROOM_UPDATE, templateName, vars);
        }
    }
}
