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
import net.furizon.backend.utils.TextUtil;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
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

    public Event() {
    }

    public Event(
        String organizer,
        String eventCode,
        String publicUrl,
        Map<String, String> eventName,
        String dateFrom,
        String dateEnd
    ) {
        slug = getSlug(organizer, eventCode);
        this.publicUrl = TextUtil.leadingSlash(publicUrl) + eventCode;
        this.eventNames = eventName;
        this.eventNamesRaw = new JSONObject(eventName).toString();
        this.dateFrom = Date.from(ZonedDateTime.parse(dateFrom).toInstant());
        this.dateEnd = Date.from(ZonedDateTime.parse(dateEnd).toInstant());
    }

    public String getEventName(String lang) {
        if (eventNames == null) {
            eventNames = new HashMap<>();
            JSONObject obj = new JSONObject(eventNamesRaw);
            for (String s : obj.keySet()) {
                eventNames.put(s, obj.getString(s));
            }
        }
        return eventNames.get(lang);
    }

    public void setSlug(String organizer, String event) {
        this.slug = getSlug(organizer, event);
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public static String getSlug(String organizer, String event) {
        return organizer + "/" + event;
    }
}
