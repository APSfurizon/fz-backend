package net.furizon.backend.infrastructure.rooms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailRoomService {
    @NotNull private final EmailSender emailSender;
    @NotNull private final RoomFinder roomFinder;

    public List<MailRequest> prepareBroadcastProblem(long roomId, @NotNull String templateName,
            TranslatableValue subject, MailVarPair... vars) {
        log.info("Sending broadcast problem to room: {}", roomId);
        List<Long> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true)
                .stream().map(RoomGuest::getUserId).toList();
        return emailSender.prepareForUsers(guests, subject, templateName, vars);
    }

    public List<MailRequest> prepareBroadcastUpdate(long roomId, @NotNull String templateName,
                                                    TranslatableValue subject, MailVarPair... vars) {
        log.info("Sending broadcast update to room: {}", roomId);
        List<Long> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true)
                .stream().map(RoomGuest::getUserId).toList();
        return emailSender.prepareForUsers(guests, subject, templateName, vars);
    }

    //Wrappers of original method
    public void fireAndForget(@NotNull MailRequest request) {
        emailSender.fireAndForget(request);
    }
    public void fireAndForgetMany(MailRequest... requests) {
        emailSender.fireAndForgetMany(requests);
    }
    public void fireAndForgetMany(@NotNull List<MailRequest> requests) {
        emailSender.fireAndForgetMany(requests);
    }

    public void prepareAndSendBroadcastProblem(long roomId, @NotNull String templateName,
                                               TranslatableValue subject, MailVarPair... vars) {
        emailSender.fireAndForgetMany(prepareBroadcastProblem(roomId, templateName, subject, vars));
    }
    public void prepareAndSendBroadcastUpdate(long roomId, @NotNull String templateName,
                                              TranslatableValue subject, MailVarPair... vars) {
        emailSender.fireAndForgetMany(prepareBroadcastUpdate(roomId, templateName, subject, vars));
    }
}
