package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "membership")
public class MembershipConfig {
    private int cardEnumerationResetMonth;
    private int cardEnumerationResetDay;

    private long minimumAgeAtEventDate;
}
