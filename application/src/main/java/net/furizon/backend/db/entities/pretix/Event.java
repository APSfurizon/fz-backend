package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.Set;

// TODO -> Replace on JOOQ

@Entity
@Table(name = "events")
@Getter
@Setter
public final class Event {
    @Id
    @Column(name = "event_slug")
    @Setter(AccessLevel.NONE)
    private String slug;

    @Column(name = "event_public_url")
    private String publicUrl;

    @Transient
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Map<String, String> eventNames;
    private String eventNamesRaw; //map lang -> name

    @Column(name = "event_date_from")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;
    @Column(name = "event_date_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnd;

    @Column(name = "event_is_current")
    private boolean isCurrentEvent;

    @OneToMany(mappedBy = "orderEvent", fetch = FetchType.LAZY)
    private Set<Order> orders;
}
