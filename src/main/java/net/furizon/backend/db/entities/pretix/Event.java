package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.utils.TextUtil;

import java.util.Map;
import java.util.Set;

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

	@Transient //TODO transform to string
	@Setter(AccessLevel.NONE)
	private Map<String, String> eventName; //map lang -> name

	@Column(name = "event_date_from")
	private String dateFrom;

	@Column(name = "event_date_end")
	private String dateEnd;

	@Column(name = "event_is_current")
	private boolean isCurrentEvent;

	@OneToMany(mappedBy = "orderEvent", fetch = FetchType.LAZY)
	private Set<Order> orders;

	public Event() {

	}

	public Event(String organizer, String eventCode, String publicUrl, Map<String, String> eventName, String dateFrom, String dateEnd){
		slug = getSlug(organizer, eventCode);
		this.publicUrl = TextUtil.leadingSlash(publicUrl) + eventCode;
		this.eventName = eventName;
		this.dateFrom = dateFrom;
		this.dateEnd = dateEnd;
	}

	public void setSlug (String organizer, String event) {
		this.slug = getSlug(organizer, event);
	}

	public void setSlug (String slug){
		this.slug = slug;
	}

	public static String getSlug(String organizer, String event){
		return organizer + "/" + event;
	}
}
