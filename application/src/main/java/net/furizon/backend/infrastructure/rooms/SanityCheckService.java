package net.furizon.backend.infrastructure.rooms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SanityCheckService {
    @NotNull private final PretixInformation pretixInformation;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final RoomLogic roomLogic;

    @Scheduled(cron = "${room.sanity-check-cronjob}")
    public List<String> runSanityChecks() {
        log.info("Running rooms sanity checks");

        List<String> errors = new LinkedList<>();
        pretixInformation.reloadCacheAndOrders();
        List<Long> roomIds = roomFinder.getRoomsForEvent(pretixInformation.getCurrentEvent().getId());
        for (long roomId : roomIds) {
            roomLogic.doSanityChecks(roomId, pretixInformation, errors);
        }
        return errors;
    }
}
