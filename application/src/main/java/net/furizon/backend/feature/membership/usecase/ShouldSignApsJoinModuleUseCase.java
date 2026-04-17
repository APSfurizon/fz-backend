package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShouldSignApsJoinModuleUseCase implements UseCase<ShouldSignApsJoinModuleUseCase.Input, Boolean> {
    @NotNull
    private final TranslationService translationService;
    @NotNull
    private final MembershipCardFinder cardFinder;
    @NotNull
    private final EventFinder eventFinder;

    @Override
    public @NotNull Boolean executor(@NotNull ShouldSignApsJoinModuleUseCase.Input input) {
        log.info("User {} is checking if {} should sign the APS join module on event {}",
                input.user.getUserId(), input.userId, input.eventId);
        Event event;
        if (input.eventId != null) {
            event = eventFinder.findEventById(input.eventId);
            if (event == null) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        translationService.error("common.event_not_found"),
                        GeneralResponseCodes.EVENT_NOT_FOUND
                );
            }
        } else {
            event = input.pretixInformation.getCurrentEvent();
        }

        var membershipCards = cardFinder.getCardsOfUserForEvent(input.userId, event);
        return membershipCards.stream().reduce(
                true,
                (i, card) -> i && card.getSignedAt() == null,
                Boolean::logicalAnd
        );
    }

    public record Input(
            long userId,
            @Nullable Long eventId,
            @NotNull PretixInformation pretixInformation,
            @NotNull FurizonUser user
    ) {}
}
