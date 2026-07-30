package net.furizon.backend.feature.pretix.objects.order.action.addPositionWithPayment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.dto.request.AddPositionWithPaymentRequest;
import net.furizon.backend.infrastructure.http.client.HttpClient;
import net.furizon.backend.infrastructure.http.client.HttpRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static net.furizon.backend.infrastructure.pretix.PretixConst.PRETIX_HTTP_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
class PretixAddPositionWithPaymentAction implements AddPositionWithPaymentAction {
    @Qualifier(PRETIX_HTTP_CLIENT)
    private final HttpClient pretixHttpClient;
    @NotNull
    private final PretixConfig pretixConfig;

    @Override
    public boolean invoke(@NotNull Order order,
                          @NotNull PretixInformation pretixInformation,

                          long itemId,
                          boolean createPayment,
                          int quantity,
                          @Nullable Long addonToPositionId,
                          @Nullable Long variationId,
                          @Nullable Long price,

                          boolean checkQuotas,
                          boolean notifyUser) {
        Event event = pretixInformation.getCurrentEvent();
        var pair = event.getOrganizerAndEventPair();

        final var request = HttpRequest.<Void>create()
                .method(HttpMethod.POST)
                .overrideBasePath(pretixConfig.getShop().getBasePath())
                .path("/{organizer}/{event}/fzbackendutils/api/add-position-and-payment/")
                .uriVariable("organizer", pair.getOrganizer())
                .uriVariable("event", pair.getEvent())
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    AddPositionWithPaymentRequest.builder()
                            .orderCode(order.getCode())
                            .item(itemId)
                            .createPayment(createPayment)
                            .quantity(quantity)
                            .addonTo(addonToPositionId)
                            .variation(variationId)
                            .price(price == null ? null : PretixGenericUtils.fromPriceToString(price, '.'))
                            .checkQuotas(checkQuotas)
                            .notifyUser(notifyUser)
                        .build()
                )
                .responseType(Void.class)
                .build();
        try {
            return pretixHttpClient.send(PretixConfig.class, request).getStatusCode().is2xxSuccessful();
        } catch (final HttpClientErrorException ex) {
            return false;
        }
    }
}
