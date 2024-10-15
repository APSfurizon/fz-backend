package net.furizon.backend.utils.pretix;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Sponsorship {
	NONE(""), SPONSOR(""), SUPER_SPONSOR(""); //These MUST be in "importance" order

	@Getter
	private final String color; //TODO for frontend

	public static Sponsorship getFromOrdinal(int ordinal){
		return Sponsorship.values()[ordinal];
	}
}
