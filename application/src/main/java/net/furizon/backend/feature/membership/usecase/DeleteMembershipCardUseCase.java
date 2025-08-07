package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.membership.action.deleteMembershipCard.DeleteMembershipCardAction;
import net.furizon.backend.feature.membership.dto.DeleteMembershipCardRequest;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMembershipCardUseCase implements UseCase<DeleteMembershipCardRequest, Boolean> {
    @NotNull private final DeleteMembershipCardAction deleteMembershipCardAction;
    @NotNull private final MembershipCardFinder cardFinder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull DeleteMembershipCardRequest input) {
        MembershipCard card = cardFinder.getMembershipCardByCardId(input.getCardId());
        if (card == null) {
            log.error("Trying deleting membership card {} but it wasn't found", input.getCardId());
            throw new ApiException(translationService.error("membership.card.not_found"),
                    AuthenticationCodes.MEMBERSHIP_NOT_FOUND);
        }

        if (card.isRegistered()) {
            log.error("Trying deleting membership card {} but it was already registered", input.getCardId());
            throw new ApiException(translationService.error("membership.card.already_registered"),
                    AuthenticationCodes.MEMBERSHIP_CARD_WAS_REGISTERED);
        }

        if (card.getCreatedForOrderId() != null) {
            log.error("Trying deleting membership card {} but it is linked to an active order", input.getCardId());
            throw new ApiException(translationService.error("membership.card.delete_fail_linked"),
                    AuthenticationCodes.MEMBERSHIP_CARD_ORDER_STILL_LINKED);
        }

        log.info("Deleting membership card {} of user {}", card.getCardNo(), card.getUserOwnerId());
        return deleteMembershipCardAction.invoke(card);
    }
}
