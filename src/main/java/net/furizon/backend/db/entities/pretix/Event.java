package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
public final class Event {
    @Id
    private String slug;

    private String publicUrl;

    @Transient //TODO transform to string
    private Map<String, String> eventName; //map lang -> name

    private String dateFrom;

    private String dateEnd;

    @Setter
    private boolean isCurrentEvent;

    @OneToMany(mappedBy = "orderEvent", fetch = FetchType.LAZY)
    private Set<Order> orders;

    public Event(
        String organizer,
        String event,
        String publicUrl,
        Map<String, String> eventName,
        String dateFrom,
        String dateEnd
    ) {
        slug = getSlug(organizer, event);
        this.publicUrl = publicUrl + "/" + slug;
        this.eventName = eventName;
        this.dateFrom = dateFrom;
        this.dateEnd = dateEnd;
    }

    public static String getSlug(String organizer, String event) {
        return organizer + "/" + event;
    }
}
