package net.furizon.backend.feature.admin.dto;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.jooq.generated.enums.ShirtSize;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class ShirtExportRow {
    @NotNull private final String orderCode;
    private final long orderSerial;
    @NotNull private final String firstName;
    @NotNull private final String lastName;
    @NotNull private final Sponsorship sponsorshipType;
    @NotNull private final ShirtSize shirtSize;
    @NotNull private final String email;
    private final long userId;
}
