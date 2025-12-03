package net.furizon.backend.feature.pretix.objects.product.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProduct;
import net.furizon.backend.feature.pretix.objects.product.PretixProductResults;
import net.furizon.backend.feature.pretix.objects.product.finder.PretixProductFinder;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.PretixPagingUtil;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

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
                if (identifier == null || !product.isActive()) {
                    log.info("Skipping item {}: identifier = {}, isActive = {}",
                            identifier, product.getIdentifier(), product.isActive());
                    return;
                }
                long productId = product.getId();
                result.itemIdToPrice().put(productId, product.getLongPrice());
                result.itemIdToBundle().put(productId, product.getBundles());

                if (identifier.startsWith(PretixConst.METADATA_EXTRA_DAYS_TAG_PREFIX)) {
                    String s = identifier.substring(PretixConst.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
                    String[] sp = s.split("_");
                    ExtraDays ed = ExtraDays.get(Integer.parseInt(sp[0]));
                    result.extraDaysIdToDay().put(productId, ed);
                    switch (sp.length) {
                        case 1: {
                            //Do nothing, we've already done in parsing
                            break;
                        }
                        case 4: {
                            String hotelName = sp[1];
                            String roomName = sp[2];
                            short capacity = Short.parseShort(sp[3]);
                            HotelCapacityPair hcPair = new HotelCapacityPair(hotelName, roomName, capacity);
                            if (ed == ExtraDays.EARLY) {
                                result.earlyDaysItemId().put(hcPair, productId);
                            } else if (ed == ExtraDays.LATE) {
                                result.lateDaysItemId().put(hcPair, productId);
                            }
                            break;
                        }
                        default: {
                            log.error("Invalid extra days identifier length: '{}' ({}) for item {}",
                                    s, sp.length, productId);
                            break;
                        }
                    }

                } else if (identifier.startsWith(PretixConst.METADATA_BOARD_ITEM_TAG_PREFIX)) {
                    String s = identifier.substring(PretixConst.METADATA_BOARD_ITEM_TAG_PREFIX.length());
                    String[] sp = s.split("_");
                    result.boardItemIds().add(productId);
                    HotelCapacityPair hcPair = sp.length == 3
                            ? new HotelCapacityPair(sp[0], sp[1], Short.parseShort(sp[2])) : null;
                    if (hcPair == null) {
                        log.warn("Parse of pretix board data: HotelCapacityPair is null (sp.length = {})", sp.length);
                    } else {
                        result.boardCapacityToItemId().put(hcPair, productId);
                    }
                    product.forEachVariationByIdentifierPrefix(
                        PretixConst.METADATA_BOARD_TAG_PREFIX,
                        (variation, strippedIdentifier, variationId) -> {
                            String[] ssp = strippedIdentifier.split("_");
                            Board board = Board.fromPretixString(ssp[0]);
                            result.variationIdToFatherItemId().put(variationId, productId);
                            result.variationIdToPrice().put(variationId, variation.getLongPrice());
                            result.boardVariationIdToType().put(variationId, board);
                            if (hcPair != null) {
                                switch (board) {
                                    case HALF: {
                                        result.halfBoardCapacityToVariationId().put(hcPair, variationId);
                                        break;
                                    }
                                    case FULL: {
                                        result.fullBoardCapacityToVariationId().put(hcPair, variationId);
                                        break;
                                    }
                                    default: break;
                                }
                            }
                        }
                    );


                } else if (identifier.startsWith(PretixConst.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
                    String s = identifier.substring(PretixConst.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
                    int day = Integer.parseInt(s);
                    result.dailyIdToDay().put(productId, day);

                } else if (identifier.startsWith(PretixConst.METADATA_ROOM_TYPE_TAG_PREFIX)) {
                    result.roomItemIds().add(productId);
                    String s = identifier.substring(PretixConst.METADATA_ROOM_TYPE_TAG_PREFIX.length());
                    if (s.equals(PretixConst.METADATA_ROOM_NO_ROOM_ITEM)) {
                        result.noRoomItemIds().add(productId);
                    } else {
                        String[] sp = s.split("_");
                        String hotelName = sp[0];
                        String roomName = sp[1];
                        short capacity = Short.parseShort(sp[2]);
                        result.roomIdToInfo().put(productId, new HotelCapacityPair(hotelName, roomName, capacity));
                        result.capacityToRoomItemIds().computeIfAbsent(capacity, k -> new ArrayList<>()).add(productId);
                        Map<String, String> names = product.getCustomNames();
                        names = names.isEmpty() ? product.getNames() : names;
                        result.roomPretixItemIdToNames().put(productId, names);
                    }


                } else {
                    switch (identifier) {
                        case PretixConst.METADATA_EVENT_TICKET: {
                            result.ticketItemIds().add(productId);
                            break;
                        }
                        case PretixConst.METADATA_MEMBERSHIP_CARD: {
                            result.membershipCardItemIds().add(productId);
                            break;
                        }
                        case PretixConst.METADATA_SPONSORSHIP: {
                            result.sponsorshipItemIds().add(productId);
                            product.forEachVariationByIdentifierPrefix(
                                PretixConst.METADATA_SPONSORSHIP_VARIATIONS_TAG_PREFIX,
                                (variation, strippedIdentifier, variationId) -> {
                                    Sponsorship ss = Sponsorship.getFromDbId(Short.parseShort(strippedIdentifier));
                                    result.variationIdToPrice().put(variationId, variation.getLongPrice());
                                    result.variationIdToFatherItemId().put(variationId, productId);
                                    result.sponsorshipIdToType().put(variationId, ss);
                                }
                            );
                            break;
                        }
                        case PretixConst.METADATA_EXTRA_FURSUIT_BADGE: {
                            result.extraFursuitsItemIds().add(productId);
                            break;
                        }

                        case PretixConst.METADATA_TEMP_ADDON: {
                            result.tempAddons().add(productId);
                            break;
                        }
                        case PretixConst.METADATA_TEMP_ITEM: {
                            result.tempItems().add(productId);
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
