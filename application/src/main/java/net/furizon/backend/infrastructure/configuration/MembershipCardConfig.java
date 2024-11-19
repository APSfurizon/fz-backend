package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "membership")
public class MembershipCardConfig {
    private int cardEnumerationResetMonth;
    private int cardEnumerationResetDay;
}
