package net.furizon.backend.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource(value = "file:/config.properties")
@ConfigurationProperties(prefix = "backend")
public class Config {
	private int port;
	private String host;
	private String secret;
}
