package net.furizon.backend.feature.pretix.objects.checkins.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.checkins.dto.CheckinList;
import net.furizon.backend.feature.pretix.objects.checkins.dto.response.GetCheckinListResponse;
import net.furizon.backend.feature.pretix.objects.checkins.finder.PretixCheckinListsFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCheckinListsUseCase implements UseCase<GetCheckinListsUseCase.Input, GetCheckinListResponse> {
    @NotNull private final PretixCheckinListsFinder pretixCheckinFinder;

    @Override
    public @NotNull GetCheckinListResponse executor(@NotNull GetCheckinListsUseCase.Input input) {
        log.info("User {} is fetching checkin lists", input.user.getUserId());
        var p = input.event.getOrganizerAndEventPair();
        List<CheckinList> checkinLists = new ArrayList<>();
        PretixPagingUtil.forEachElement(
            paging -> pretixCheckinFinder.getPagedCheckinLists(p.getOrganizer(), p.getEvent(), paging),
            result -> checkinLists.add(result.getLeft().toCheckinList())
        );
        return new GetCheckinListResponse(checkinLists);
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull Event event
    ) {}
}
