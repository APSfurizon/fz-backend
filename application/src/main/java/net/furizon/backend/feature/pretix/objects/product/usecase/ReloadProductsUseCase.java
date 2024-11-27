package net.furizon.backend.feature.pretix.objects.product.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
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

        PretixPagingUtil.forEachElement(
            paged -> pretixProductFinder.getPagedProducts(pair.getOrganizer(), pair.getEvent(), paged),
            r -> {
                PretixProduct product = r.getLeft();
                String identifier = product.getIdentifier();
                if (identifier == null) {
                    return;
                }

                if (identifier.startsWith(Const.METADATA_EXTRA_DAYS_TAG_PREFIX)) {
                    String s = identifier.substring(Const.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
                    ExtraDays ed = ExtraDays.get(Integer.parseInt(s));
                    result.extraDaysIdToDay().put(product.getId(), ed);

                } else if (identifier.startsWith(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
                    String s = identifier.substring(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
                    int day = Integer.parseInt(s);
                    result.dailyIdToDay().put(product.getId(), day);

                } else if (identifier.startsWith(Const.METADATA_ROOM_TYPE_TAG_PREFIX)) {
                    result.roomItemIds().add(product.getId());
                    String s = identifier.substring(Const.METADATA_ROOM_TYPE_TAG_PREFIX.length());
                    if (s.equals(Const.METADATA_ROOM_NO_ROOM_VARIATION)) {
                        result.noRoomVariationIds().add(product.getId());
                    } else {
                        String[] sp = s.split("_");
                        String hotelName = sp[0];
                        short capacity = Short.parseShort(sp[1]);
                        HotelCapacityPair p = new HotelCapacityPair(hotelName, capacity);
                        result.roomIdToInfo().put(product.getId(), p);
                        result.roomInfoToNames().put(p, product.getNames());
                    }


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
                                    Sponsorship ss = Sponsorship.get(Integer.parseInt(strippedIdentifier));
                                    result.sponsorshipIdToType().put(variation.getId(), ss);
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
        );
        return result;
    }
}
