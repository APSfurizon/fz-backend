package net.furizon.backend.feature.fursuits.action.createFursuit;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.fursuits.action.bringFursuitToEvent.UpdateBringFursuitToEventAction;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static net.furizon.jooq.generated.tables.Fursuits.FURSUITS;

@Component
@RequiredArgsConstructor
public class JooqCreateFursuitAction implements CreateFursuitAction {
    @NotNull private final UpdateBringFursuitToEventAction updateBringFursuitToEventAction;
    @NotNull private final SqlCommand command;

    @Override
    @Transactional
    public long invoke(long ownerId, @NotNull String name, @NotNull String species, boolean showInFursuitCount, @Nullable Order linkedOrder) {
        long fursuitId = command.executeResult(
            PostgresDSL.insertInto(
                FURSUITS,
                FURSUITS.FURSUIT_NAME,
                FURSUITS.FURSUIT_SPECIES,
                FURSUITS.SHOW_IN_FURSUITCOUNT,
                FURSUITS.USER_ID
            )
            .values(
                name,
                species,
                showInFursuitCount,
                ownerId
            )
            .returning(FURSUITS.FURSUIT_ID)
        ).getFirst().get(FURSUITS.FURSUIT_ID);

        if (linkedOrder != null) {
            updateBringFursuitToEventAction.invoke(
                    fursuitId,
                    true,
                    linkedOrder
            );
        }

        return fursuitId;
    }
}
