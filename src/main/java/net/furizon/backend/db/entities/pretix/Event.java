package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class Event {
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
