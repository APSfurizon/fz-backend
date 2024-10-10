package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.utils.TextUtil;

import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "events")
public final class Event {

	@Id @Getter
	@Column(name = "event_slug")
	private String slug;

	@Getter @Setter
	@Column(name = "event_public_url")
	private String publicUrl;

	@Getter @Transient //TODO transform to string
	private Map<String, String> eventName; //map lang -> name

	@Getter @Setter
	@Column(name = "event_date_from")
	private String dateFrom;

	@Getter @Setter
	@Column(name = "event_date_end")
	private String dateEnd;

	@Getter @Setter
	@Column(name = "event_is_current")
	private boolean isCurrentEvent;

	@Getter @Setter
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
