package net.furizon.backend.db.entities.pretix.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines connection settings to Pretix's rest api
 */
@Entity
@Table
public class PretixConnectionSettings {
	@Getter
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Getter @Setter
	@Column(nullable = false)
	private int maxRetries;

	@Getter @Setter
	@Column(nullable = false)
	private int maxHttpConnections;

	@Getter @Setter
	@Column(nullable = false)
	private int timeout;
}
