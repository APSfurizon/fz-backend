package net.furizon.backend.db.entities.pretix.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines Pretix's profile picture settings, from the organizer to the connection data
 */
@Entity @Table
public final class PretixPropicSettings {

	@Getter @Id @Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Getter @Setter @Column(nullable = false)
	private boolean enabled;

	@Getter @Setter @Column(nullable = false)
	private long deadlineTs;

	@Getter @Setter @Column(nullable = false)
	private long maxFileSizeBytes;

	@Getter @Setter @Column(nullable = false)
	private long maxSizeX;

	@Getter @Setter @Column(nullable = false)
	private long maxSizeY;

	@Getter @Setter @Column(nullable = false)
	private long minSizeX;

	@Getter @Setter @Column(nullable = false)
	private long minSizeY;
}

