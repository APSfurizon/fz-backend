package net.furizon.backend.infrastructure.rooms;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.room.action.deleteExpiredExchange.DeleteExpiredExchangesAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteExpiredExchangeService {
    @NotNull private final DeleteExpiredExchangesAction deleteExpiredExchangesAction;

    @Scheduled(cron = "${room.exchanges.delete-expired-cronjob}")
    public void deleteExpiredExchanges() {
        log.info("Deleting expired exchanges");
        int res = deleteExpiredExchangesAction.invoke();
        log.info("Deleted {} expired exchanges", res);
    }
}
