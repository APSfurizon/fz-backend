package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Set;

@Entity
@Table(name = "events")
public final class Event {
	@Id
	@Getter
	private String slug;

	@Getter
	private String publicUrl;

	@Getter
	private HashMap<String, String> eventName; //map lang -> name

	@Getter
	private String dateFrom;

	@Getter @Setter
	private boolean isCurrentEvent;

	@OneToMany(mappedBy = "orderEvent")
	private Set<Order> orders;

	public Event(String organizer, String event, String publicUrl, HashMap<String, String> eventName, String dateFrom){
		slug = getSlug(organizer, event);
		this.publicUrl = publicUrl + "/" + slug;
		this.eventName = eventName;
		this.dateFrom = dateFrom;
	}

	public static String getSlug(String organizer, String event){
		return organizer + "/" + event;
	}
}
