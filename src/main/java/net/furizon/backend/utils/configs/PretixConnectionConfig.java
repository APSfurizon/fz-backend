package net.furizon.backend.utils.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource(value = "file:config.properties")
@ConfigurationProperties(prefix = "pretix.connection")
public class PretixConnectionConfig {


}
