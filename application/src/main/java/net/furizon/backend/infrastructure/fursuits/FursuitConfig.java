package net.furizon.backend.infrastructure.fursuits;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fursuits")
public class FursuitConfig {
    private final short defaultFursuitsNo;
    private final short maxExtraFursuits;
}
