package net.furizon.backend.feature.pretix.product.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.event.Event;
import net.furizon.backend.feature.pretix.product.PretixProduct;
import net.furizon.backend.feature.pretix.product.PretixProductResults;
import net.furizon.backend.feature.pretix.product.finder.PretixProductFinder;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReloadProductsUseCase implements UseCase<Event, PretixProductResults> {
    private final PretixProductFinder pretixProductFinder;

    @NotNull
    @Override
    public PretixProductResults executor(Event event) {
        final var pair = event.getOrganizerAndEventPair();
        PretixProductResults result = new PretixProductResults();

        PretixPagingUtil.forEachPage(
            paged -> pretixProductFinder.getPagedProducts(pair.getFirst(), pair.getSecond(), paged),
            r -> {
                for (final PretixProduct product : r.getFirst()) {
                    String identifier = product.getIdentifier();
                    if (identifier == null) {
                        continue;
                    }

                    if (identifier.startsWith(Const.METADATA_EXTRA_DAYS_TAG_PREFIX)) {
                        String s = identifier.substring(Const.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
                        ExtraDays ed = ExtraDays.valueOf(s);
                        result.extraDaysIdToDay().put(product.getId(), ed);

                    } else if (identifier.startsWith(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
                        String s = identifier.substring(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
                        int day = Integer.parseInt(s);
                        result.dailyIdToDay().put(product.getId(), day);

                    } else {
                        switch (identifier) {
                            case Const.METADATA_EVENT_TICKET: {
                                result.ticketItemIds().add(product.getId());
                                break;
                            }
                            case Const.METADATA_MEMBERSHIP_CARD: {
                                result.membershipCardItemIds().add(product.getId());
                                break;
                            }
                            case Const.METADATA_SPONSORSHIP: {
                                result.sponsorshipItemIds().add(product.getId());
                                product.forEachVariationByIdentifierPrefix(
                                    Const.METADATA_SPONSORSHIP_VARIATIONS_TAG_PREFIX,
                                    (variation, strippedIdentifier) -> {
                                        Sponsorship ss = Sponsorship.valueOf(strippedIdentifier);
                                        result.sponsorshipIdToType().put(variation.getId(), ss);
                                    }
                                );
                                break;
                            }
                            case Const.METADATA_ROOM: {
                                result.roomItemIds().add(product.getId());
                                product.forEachVariationByIdentifierPrefix(
                                    Const.METADATA_ROOM_TYPE_TAG_PREFIX,
                                    (variation, strippedIdentifier) -> {
                                        String[] sp = strippedIdentifier.split("_");
                                        String hotelName = sp[0];
                                        int capacity = Integer.parseInt(sp[1]);
                                        Pair<Integer, String> p = Pair.of(capacity, hotelName);
                                        result.roomIdToInfo().put(variation.getId(), p);
                                        result.roomInfoToName().put(p, variation.getName());
                                    }
                                );
                                break;
                            }

                            default: {
                                log.warn(
                                        "Unrecognized identifier while parsing product ({}) :'{}'",
                                        product.getId(), identifier
                                );
                                break;
                            }
                        }
                    }
                }
            }
        );
        return result;
    }
}
