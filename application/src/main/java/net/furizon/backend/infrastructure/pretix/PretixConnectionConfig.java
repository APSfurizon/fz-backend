package net.furizon.backend.infrastructure.pretix;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pretix.connection")
public class PretixConnectionConfig {


}
