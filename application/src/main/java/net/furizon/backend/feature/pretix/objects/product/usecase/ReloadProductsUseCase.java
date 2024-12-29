package net.furizon.backend.feature.pretix.objects.product.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.infrastructure.pretix.Const;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
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
                long productId = product.getId();
                result.itemIdToPrice().put(productId, PretixGenericUtils.fromStrPriceToLong(product.getPrice()));

                if (identifier.startsWith(Const.METADATA_EXTRA_DAYS_TAG_PREFIX)) {
                    String s = identifier.substring(Const.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
                    String[] sp = s.split("_");
                    ExtraDays ed = ExtraDays.get(Integer.parseInt(sp[0]));
                    String hotelName = sp[1];
                    short capacity = Short.parseShort(sp[2]);
                    result.extraDaysIdToDay().put(productId, ed);
                    HotelCapacityPair hcPair = new HotelCapacityPair(hotelName, capacity);
                    if (ed == ExtraDays.EARLY) {
                        result.earlyDaysItemId().put(hcPair, productId);
                    } else if (ed == ExtraDays.LATE) {
                        result.lateDaysItemId().put(hcPair, productId);
                    }

                } else if (identifier.startsWith(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
                    String s = identifier.substring(Const.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
                    int day = Integer.parseInt(s);
                    result.dailyIdToDay().put(productId, day);

                } else if (identifier.startsWith(Const.METADATA_ROOM_TYPE_TAG_PREFIX)) {
                    result.roomItemIds().add(productId);
                    String s = identifier.substring(Const.METADATA_ROOM_TYPE_TAG_PREFIX.length());
                    if (s.equals(Const.METADATA_ROOM_NO_ROOM_ITEM)) {
                        result.noRoomItemIds().add(productId);
                    } else {
                        String[] sp = s.split("_");
                        String hotelName = sp[0];
                        short capacity = Short.parseShort(sp[1]);
                        result.roomIdToInfo().put(productId, new HotelCapacityPair(hotelName, capacity));
                        result.roomPretixItemIdToNames().put(productId, product.getNames());
                    }


                } else {
                    switch (identifier) {
                        case Const.METADATA_EVENT_TICKET: {
                            result.ticketItemIds().add(productId);
                            break;
                        }
                        case Const.METADATA_MEMBERSHIP_CARD: {
                            result.membershipCardItemIds().add(productId);
                            break;
                        }
                        case Const.METADATA_SPONSORSHIP: {
                            result.sponsorshipItemIds().add(productId);
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
