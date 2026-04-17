package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "membership")
public class MembershipConfig {
    private final int cardEnumerationResetMonth;
    private final int cardEnumerationResetDay;

    private final long minimumAgeAtEventDate;

    @NotNull private final String apsJoinModuleItJteFilename;
    @NotNull private final String apsJoinModuleEnJteFilename;
}
