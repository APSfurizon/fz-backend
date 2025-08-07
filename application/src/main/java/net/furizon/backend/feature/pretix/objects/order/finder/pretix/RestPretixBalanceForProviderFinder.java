package net.furizon.backend.feature.pretix.objects.order.finder.pretix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.feature.pretix.objects.payment.finder.PretixPaymentFinder;
import net.furizon.backend.feature.pretix.objects.refund.PretixRefund;
import net.furizon.backend.feature.pretix.objects.refund.finder.PretixRefundFinder;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestPretixBalanceForProviderFinder implements PretixBalanceForProviderFinder {
    @NotNull private final PretixRefundFinder pretixRefundFinder;
    @NotNull private final PretixPaymentFinder pretixPaymentFinder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Map<String, Long> get(@NotNull String orderCode, @NotNull Event event,
                                          boolean crashOnInvalidState) {
        List<PretixPayment> payments = pretixPaymentFinder.getPaymentsForOrder(event, orderCode);
        List<PretixRefund> refunds = pretixRefundFinder.getRefundsForOrder(event, orderCode);
        return get(payments, refunds, orderCode, event, crashOnInvalidState);
    }

    @Override
    public @NotNull Map<String, Long> get(@NotNull List<PretixPayment> payments, @NotNull List<PretixRefund> refunds,
                                      @NotNull String orderCode, @NotNull Event event, boolean crashOnInvalidState) {
        Map<String, Long> balanceForProvider = new HashMap<>();

        //For each payment provider, calc how much the user has paid with them
        for (PretixPayment payment : payments) {
            PretixPayment.PaymentState state = payment.getState();
            if (state == PretixPayment.PaymentState.CONFIRMED) {
                String provider = payment.getProvider();
                Long balance = balanceForProvider.get(provider);
                if (balance == null) {
                    balance = 0L;
                }

                balance += PretixGenericUtils.fromStrPriceToLong(payment.getAmount());
                balanceForProvider.put(provider, balance);

            } else if (state == PretixPayment.PaymentState.PENDING || state == PretixPayment.PaymentState.CREATED) {
                log.error("No payments for order {} on event {} "
                                + "can be in status PENDING or CREATED. Failed payment: {} {}",
                        orderCode, event, payment.getId(), state);
                if (crashOnInvalidState) {
                    throw new ApiException(translationService.error("order.payment_illegal_state",
                            new Object[]{payment.getId(), orderCode}),
                            GeneralResponseCodes.ORDER_PAYMENTS_STILL_PENDING);
                }
            }
        }

        //For each payment provider, subtract how much it was already refunded from it
        for (PretixRefund refund : refunds) {
            PretixRefund.RefundState state = refund.getState();
            if (state == PretixRefund.RefundState.DONE) {
                String provider = refund.getProvider();
                Long balance = balanceForProvider.get(provider);
                if (balance == null) {
                    balance = 0L;
                }

                balance -= PretixGenericUtils.fromStrPriceToLong(refund.getAmount());
                balanceForProvider.put(provider, balance);

            } else if (
                    state == PretixRefund.RefundState.CREATED
                            || state == PretixRefund.RefundState.TRANSIT
                            || state == PretixRefund.RefundState.EXTERNAL
            ) {
                log.error("No refunds for order {} on event {} "
                                + "can be in status CREATED, TRANSIT or EXTERNAL. Failed refund: {} {}",
                        orderCode, event, refund.getId(), state);
                if (crashOnInvalidState) {
                    throw new ApiException(translationService.error("order.refund_illegal_state",
                            new Object[]{refund.getId(), orderCode}),
                            GeneralResponseCodes.ORDER_REFUNDS_STILL_PENDING);
                }
            }
        }

        return balanceForProvider;
    }
}
