package net.furizon.backend.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class UserDisplayDataWithExtraDays {
    @NotNull private final UserDisplayData user;
    @NotNull private ExtraDays extraDays;
}
