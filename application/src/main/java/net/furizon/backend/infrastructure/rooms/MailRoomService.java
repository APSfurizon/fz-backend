package net.furizon.backend.infrastructure.rooms;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.dto.RoomGuest;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailRoomService {
    @NotNull private final EmailSender emailSender;
    @NotNull private final RoomFinder roomFinder;

    public List<MailRequest> prepareProblem(MailRequest... requests) {
        for (MailRequest request : requests) {
            request.subject(RoomEmailTexts.SUBJECT_ROOM_PROBLEM);
        }
        return new ArrayList<MailRequest>(Arrays.asList(requests));
    }

    public List<MailRequest> prepareUpdate(MailRequest... requests) {
        for (MailRequest request : requests) {
            request.subject(RoomEmailTexts.SUBJECT_ROOM_UPDATE);
        }
        return new ArrayList<MailRequest>(Arrays.asList(requests));
    }

    public List<MailRequest> prepareBroadcastProblem(long roomId, @NotNull String templateName, MailVarPair... vars) {
        log.info("Sending broadcast problem to room: {}", roomId);
        List<Long> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true).stream().map(RoomGuest::getUserId).toList();
        return emailSender.prepareForUsers(guests, RoomEmailTexts.SUBJECT_ROOM_PROBLEM, templateName, vars);
    }

    public List<MailRequest> prepareBroadcastUpdate(long roomId, @NotNull String templateName, MailVarPair... vars) {
        log.info("Sending broadcast update to room: {}", roomId);
        List<Long> guests = roomFinder.getRoomGuestsFromRoomId(roomId, true).stream().map(RoomGuest::getUserId).toList();
        return emailSender.prepareForUsers(guests, RoomEmailTexts.SUBJECT_ROOM_UPDATE, templateName, vars);
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


    public void prepareAndSendProblem(MailRequest... requests) {
        emailSender.fireAndForgetMany(prepareProblem(requests));
    }
    public void prepareAndSendUpdate(MailRequest... requests) {
        emailSender.fireAndForgetMany(prepareUpdate(requests));
    }

    public void prepareAndSendBroadcastProblem(long roomId, @NotNull String templateName, MailVarPair... vars) {
        emailSender.fireAndForgetMany(prepareBroadcastProblem(roomId, templateName, vars));
    }
    public void prepareAndSendBroadcastUpdate(long roomId, @NotNull String templateName, MailVarPair... vars) {
        emailSender.fireAndForgetMany(prepareBroadcastUpdate(roomId, templateName, vars));
    }
}
