package net.furizon.backend.db.entities.pretix.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Setter;

/**
 * Defines connection settings to Pretix's rest api
 */
@Entity
@Table
@lombok.Data
public class PretixConnectionSettings {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(nullable = false)
    private int maxRetries;

    @Column(nullable = false)
    private int maxHttpConnections;

    @Column(nullable = false)
    private int timeout;
}
