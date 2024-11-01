package net.furizon.backend.feature.pretix.order.controller;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.order.finder.OrderFinder;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import net.furizon.backend.service.pretix.PretixService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class TestOrderController {
    private final PushPretixAnswerAction pushPretixAnswerAction;

    private final OrderFinder orderFinder;

    private final PretixInformation pretixService;

    @PutMapping("/{code}")
    public Boolean pushAnswer(@PathVariable @NotNull String code) {
        final var order = orderFinder.findOrderByCode(code);
        if (order == null) {
            throw new ApiException("No order found with code " + code);
        }

        return pushPretixAnswerAction.invoke(order, pretixService);
    }
}
