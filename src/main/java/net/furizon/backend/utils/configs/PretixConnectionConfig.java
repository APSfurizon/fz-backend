package net.furizon.backend.utils.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pretix.connection")
public class PretixConnectionConfig {


}
