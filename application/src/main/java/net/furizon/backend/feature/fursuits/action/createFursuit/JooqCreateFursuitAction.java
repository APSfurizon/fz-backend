package net.furizon.backend.feature.fursuits.action.createFursuit;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;
import static net.furizon.jooq.generated.tables.FursuitsOrders.FURSUITS_ORDERS;

@Component
@RequiredArgsConstructor
public class JooqCreateFursuitAction implements CreateFursuitAction {
    @NotNull private final SqlCommand command;

    @Override
    @Transactional
    public long invoke(long ownerId, @NotNull String name, @NotNull String species, @Nullable Order linkedOrder) {
        long fursuitId = command.executeResult(
            PostgresDSL.insertInto(
                FURSUITS,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                FURSUITS.USER_ID
            )
            .values(
                name,
                species,
                ownerId
            )
            .returning(FURSUITS.FURSUIT_ID)
        ).getFirst().get(FURSUITS.FURSUIT_ID);

        if (linkedOrder != null) {
            command.execute(
                PostgresDSL.insertInto(
                    FURSUITS_ORDERS,
                    FURSUITS_ORDERS.FURSUIT_ID,
                    FURSUITS_ORDERS.ORDER_ID
                )
                .values(
                    fursuitId,
                    linkedOrder.getId()
                )
            );
        }

        return fursuitId;
    }
}
