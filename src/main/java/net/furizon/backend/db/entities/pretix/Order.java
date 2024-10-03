package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.pretix.ExtraDays;
import net.furizon.backend.pretix.Sponsorship;

import javax.naming.Name;
import java.util.Arrays;
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

	@Id
	@Getter
	private String code;


	private String dailyDays; //TODO convert
	@Getter @Transient
	private Set<String> days = new HashSet<String>();

	@Getter
	private Sponsorship sponsorship = Sponsorship.NONE;

	@Getter
	private ExtraDays extraDays = ExtraDays.NONE;

	@Getter
	private int roomCapacity = 0; // 0 = has no room

	@Getter
	private String hotelLocation;

	@Getter
	private boolean hasMembership;

	public Order(){}
	public Order(String code, Set<String> days, Sponsorship sponsorship, ExtraDays extraDays, int roomCapacity, String hotelLocation, boolean hasMembership) {
		this.code = code;
		this.days = days;
		this.extraDays = extraDays;
		this.sponsorship = sponsorship;
		this.roomCapacity = roomCapacity;
		this.hotelLocation  = hotelLocation;
		this.hasMembership = hasMembership;
	}

	//DON'T USE THAT!
	public void setDailyDays(String dailyDays) {
		this.dailyDays = dailyDays;
		days.addAll(Arrays.asList(dailyDays.split(",")));
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
