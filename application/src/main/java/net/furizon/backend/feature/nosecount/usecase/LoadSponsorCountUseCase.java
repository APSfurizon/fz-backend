package net.furizon.backend.feature.nosecount.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.responses.SponsorCountResponse;
import net.furizon.backend.feature.nosecount.finder.CountsFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoadSponsorCountUseCase implements UseCase<Long, SponsorCountResponse> {
    @NotNull private final CountsFinder countsFinder;

    @Override
    public @NotNull SponsorCountResponse executor(@NotNull Long eventId) {
        List<UserDisplayData> users = countsFinder.getSponsors(eventId);

        Map<Sponsorship, List<UserDisplayData>> results = new HashMap<>();
        for (UserDisplayData user : users) {
            Sponsorship sponsorship = user.getSponsorship();
            List<UserDisplayData> list = results.computeIfAbsent(sponsorship, k -> new ArrayList<>());
            list.add(user);
        }

        return new SponsorCountResponse(results);
    }
}
