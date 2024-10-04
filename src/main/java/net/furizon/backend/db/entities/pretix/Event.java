package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "events")
public final class Event {
	@Id
	@Getter
	private String slug;

	@Getter
	private String publicUrl;

	@Getter @Transient //TODO transform to string
	private Map<String, String> eventName; //map lang -> name

	@Getter
	private String dateFrom, dateEnd;

	@Getter @Setter
	private boolean isCurrentEvent;

	@Getter
	@OneToMany(mappedBy = "orderEvent", fetch = FetchType.LAZY)
	private Set<Order> orders;

	public Event(String organizer, String event, String publicUrl, Map<String, String> eventName, String dateFrom, String dateEnd){
		slug = getSlug(organizer, event);
		this.publicUrl = publicUrl + "/" + slug;
		this.eventName = eventName;
		this.dateFrom = dateFrom;
		this.dateEnd = dateEnd;
	}

	public static String getSlug(String organizer, String event){
		return organizer + "/" + event;
	}
}
