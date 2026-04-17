package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.membership.action.markCardsAsSigned.MarkCardsAsSignedAction;
import net.furizon.backend.feature.membership.dto.ApsRegistrationTemplateVars;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.membership.finder.PersonalInfoFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.MembershipConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.templating.service.CustomTemplateService;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateApsSignHtmlUseCase implements UseCase<GenerateApsSignHtmlUseCase.Input, String> {
    @NotNull
    private final CustomTemplateService customTemplateService;
    @NotNull
    private final MembershipConfig config;

    @NotNull
    private final GeneralChecks generalChecks;

    @NotNull
    private final TranslationService translationService;

    @NotNull
    private final MarkCardsAsSignedAction markCardsAsSignedAction;

    @NotNull
    private final UserFinder userFinder;
    @NotNull
    private final EventFinder eventFinder;
    @NotNull
    private final PersonalInfoFinder personalInfoFinder;
    @NotNull
    private final MembershipCardFinder membershipCardFinder;



    @Override
    @Transactional
    public @NotNull String executor(@NotNull GenerateApsSignHtmlUseCase.Input input) {
        log.info("User {} is generating APS join module for user {} on event {}",
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

        var user = generalChecks.assertUserFound(userFinder.getMailDataForUser(input.userId));
        var pui = generalChecks.assertUserFound(personalInfoFinder.findByUserId(input.userId));

        var memberships = membershipCardFinder.getCardsOfUserForEvent(input.userId, event);
        if (memberships.isEmpty()) {
            log.error("User {} doesn't have any membership card", input.userId);
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    translationService.error("membership.card.not_found"),
                    AuthenticationCodes.MEMBERSHIP_NOT_FOUND
            );
        }
        boolean res = markCardsAsSignedAction.invoke(
                memberships.stream().map(MembershipCard::getCardId).toList()
        );
        if (!res) {
            log.error("Could not mark membership cards as signed for user {}", input.userId);
            throw new ApiException(
                    translationService.error("common.server_error"),
                    GeneralResponseCodes.GENERIC_ERROR
            );
        }

        return customTemplateService.renderTemplate(
            (user.getLanguage().equals(Locale.ITALY) || user.getLanguage().equals(Locale.ITALIAN))
                 ? config.getApsJoinModuleItJteFilename()
                 : config.getApsJoinModuleEnJteFilename(),
            Map.ofEntries(
                Map.entry(
                        ApsRegistrationTemplateVars.CARD_NO.getVarName(),
                        String.join(", ", memberships.getFirst().getCardNo())
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.FULL_NAME.getVarName(),
                        String.format("%s %s", pui.getFirstName(), pui.getLastName())
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.FISCAL_CODE.getVarName(),
                        pui.getFiscalCode() == null ? "" : pui.getFiscalCode()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.BIRTH_CITY.getVarName(),
                        pui.getBirthCity()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.BIRTH_DAY.getVarName(),
                        pui.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE)
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.CITY.getVarName(),
                        pui.getResidenceCity()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.REGION.getVarName(),
                        pui.getResidenceRegion() == null ? "" : pui.getResidenceRegion()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.ADDRESS.getVarName(),
                        pui.getResidenceAddress()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.ZIP_CODE.getVarName(),
                        pui.getResidenceZipCode()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.EMAIL.getVarName(),
                        user.getEmail()
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.PHONE_NO.getVarName(),
                        String.format("%s %s", pui.getPrefixPhoneNumber(), pui.getPhoneNumber())
                ),
                Map.entry(
                        ApsRegistrationTemplateVars.TODAY_DATE.getVarName(),
                        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            )
        );
    }

    public record Input(
            long userId,
            @Nullable Long eventId,
            @NotNull PretixInformation pretixInformation,
            @NotNull FurizonUser user
    ) {}
}
