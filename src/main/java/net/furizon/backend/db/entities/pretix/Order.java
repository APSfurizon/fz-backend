package net.furizon.backend.db.entities.pretix;

import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.pretix.Sponsorship;

import java.util.HashSet;
import java.util.Set;

/*
- Che prodotti ha
	- SE ha un biglietto per l'evento
	- Tipo di sponsor
	- Se daily: che giorni ha
	- SE ha una stanza; tipo della stanza
- Secret per collegare l'ordine all'utente
 */
public class Order {

	@Getter
	private Set<String> days = new HashSet<String>();
	@Getter
	private Sponsorship sponsorship;
	@Getter
	private int roomCapacity = 0; // 0 = has no room

	public Order(Set<String> days, Sponsorship sponsorship, int roomCapacity) {
		this.days = days;
		this.sponsorship = sponsorship;
		this.roomCapacity = roomCapacity;
	}

	//TODO User
	//TODO Event

	public boolean isDaily(){
		return !days.isEmpty();
	}

	public boolean ownsRoom(){
		return roomCapacity > 0;
	}
}
