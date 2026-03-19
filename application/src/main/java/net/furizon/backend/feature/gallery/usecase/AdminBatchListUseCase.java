package net.furizon.backend.feature.gallery.usecase;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.response.ListUploadsResponse;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBatchListUseCase implements UseCase<FurizonUser, ListUploadsResponse> {
    @NotNull
    private final Cache<Long, Long> userToLastMediaId = Caffeine.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .build();
    @NotNull
    private final Cache<Long, Long> userToLastSize = Caffeine.newBuilder()
            .expireAfterWrite(7L, TimeUnit.DAYS)
            .build();

    @Override
    public @NonNull ListUploadsResponse executor(@NonNull FurizonUser input) {
        boolean firstRequest = false; //TODO TAKE THIS AS INPUT
        long batchLimit = 100; //TODO TAKE THIS AS A CONFIG

        long userId = input.getUserId();
        long from = 0L;
        long limit = 0L;

        if (firstRequest) {

        } else {
            //Get how many contiguous medias from the lastMediaId the current user
            // has reserved previously. If it's == 0, it means that the user is still
            // catching 
            long lastUserSize = userToLastSize.get(userId, l -> 0L);
            if (lastUserSize > 0L) {

                Map.Entry<Long, Long> maxUser = Collections.max(
                        userToLastMediaId.asMap().entrySet(),
                        Map.Entry.comparingByValue()
                );
                long maxUserUploadId = maxUser.getValue();
                long maxUserSize = userToLastSize.get(maxUser.getKey(), l -> 0L);

                from = maxUserUploadId + maxUserSize;
                limit = batchLimit;

            } else {

                from = userToLastMediaId.get(userId, l -> 0L);
                limit = batchLimit;
            }
        }




        return null;
    }
}
