package net.furizon.backend.pretix.configuration;

import net.furizon.backend.pretix.property.PretixProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PretixProperties.class)
class PretixConfiguration {
}
