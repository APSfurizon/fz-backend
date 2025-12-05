package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.admin.dto.JooqRoomNotFullRow;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.admin.ReminderEmailTexts;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.LANG_PRETIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmptyRoomReminderUseCase implements UseCase<PretixInformation, Integer> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final RoomConfig roomConfig;
    @NotNull private final FrontendConfig frontendConfig;

    @Override
    public @NotNull Integer executor(@NotNull PretixInformation pretixInformation) {
        int n = 0;
        log.info("Sending room not full reminder emails");

        if (roomLogic.isConfirmationSupported()) {
            log.warn("Early exit from EmptyRoomReminder email: RoomLogic supports confirmation");
            return 0;
        }

        String deadlineStr = "event starts";
        OffsetDateTime deadline = roomConfig.getRoomChangesEndTime();
        if (deadline != null) {
            deadlineStr = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        var data = roomFinder.getNotFullRooms(pretixInformation.getCurrentEvent().getId(), pretixInformation);
        Map<Long, JooqRoomNotFullRow> userIdToObj = data.stream().collect(
                Collectors.toMap(JooqRoomNotFullRow::getUserId, Function.identity())
        );

        List<UserEmailData> userEmails = userFinder.getMailDataForUsers(userIdToObj.keySet().stream().toList());
        MailRequest[] mails = new MailRequest[userEmails.size()];
        for (UserEmailData mail : userEmails) {
            JooqRoomNotFullRow obj = userIdToObj.get(mail.getUserId());
            if (obj == null) {
                continue;
            }

            Map<String, String> names = pretixInformation.getItemNames(obj.getRoomPretixItemId());
            String roomTypeName = names != null ? names.get(LANG_PRETIX) : "";

            if (obj.getRoomCount() > 0) {
                mails[n++] = new MailRequest(mail, ReminderEmailTexts.TEMPLATE_ROOM_NOT_FULL,
                        MailVarPair.of(EmailVars.LINK, frontendConfig.getRoomPageUrl()),
                        MailVarPair.of(EmailVars.DEADLINE, deadlineStr),
                        MailVarPair.of(EmailVars.ROOM_TYPE_NAME, roomTypeName),
                        MailVarPair.of(EmailVars.ROOM_CAPACITY, String.valueOf(obj.getRoomCapacity())),
                        MailVarPair.of(EmailVars.ROOM_CURRENT_GUEST_NO, String.valueOf(obj.getRoomCount()))
                ).subject("mail.reminder_room_not_full.title");
            } else {
                mails[n++] = new MailRequest(mail, ReminderEmailTexts.TEMPLATE_ROOM_NOT_CREATED,
                        MailVarPair.of(EmailVars.LINK, frontendConfig.getRoomPageUrl()),
                        MailVarPair.of(EmailVars.DEADLINE, deadlineStr),
                        MailVarPair.of(EmailVars.ROOM_TYPE_NAME, roomTypeName)
                ).subject("mail.reminder_room_not_created.title");
            }
        }

        log.info("Firing room not full reminder emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
