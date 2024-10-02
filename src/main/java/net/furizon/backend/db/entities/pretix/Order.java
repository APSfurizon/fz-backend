package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.pretix.Sponsorship;

import javax.naming.Name;
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
@Entity
@Table(name = "orders")
public class Order {

	@Getter
	@Transient
	private Set<String> days = new HashSet<String>();

	@Getter @Transient
	private Sponsorship sponsorship;

	@Getter @Transient
	private int roomCapacity = 0; // 0 = has no room

	public Order(Set<String> days, Sponsorship sponsorship, int roomCapacity) {
		this.days = days;
		this.sponsorship = sponsorship;
		this.roomCapacity = roomCapacity;
	}

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Getter
	private User orderOwner;

	@ManyToOne
	@JoinColumn(name = "event_id")
	@Getter
	private Event orderEvent;

	public boolean isDaily(){
		return !days.isEmpty();
	}

	public boolean ownsRoom(){
		return roomCapacity > 0;
	}
}
