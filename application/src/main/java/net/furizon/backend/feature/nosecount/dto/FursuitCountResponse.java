package net.furizon.backend.feature.nosecount.dto;

import lombok.Data;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class FursuitCountResponse {
    @NotNull private final List<FursuitDisplayData> fursuits;
}
