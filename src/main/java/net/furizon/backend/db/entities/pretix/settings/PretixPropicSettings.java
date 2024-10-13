package net.furizon.backend.db.entities.pretix.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines Pretix's profile picture settings, from the organizer to the connection data
 */
@Entity
@Table
@Data
public final class PretixPropicSettings {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private long deadlineTs;

    @Column(nullable = false)
    private long maxFileSizeBytes;

    @Column(nullable = false)
    private long maxSizeX;

    @Column(nullable = false)
    private long maxSizeY;

    @Getter
    @Setter
    @Column(nullable = false)
    private long minSizeX;

    @Column(nullable = false)
    private long minSizeY;
}
