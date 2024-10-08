package net.furizon.backend.utils.configs;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource(value = "file:config.properties")
@ConfigurationProperties(prefix = "backend")
public class BackendConfig {
	@Getter
	private int port;

	@Getter
	private String host;

	@Getter
	private String secret;
}
