package net.furizon.backend.pretix;

import lombok.Getter;

public enum Sponsorship {
	NONE(""), SPONSOR(""), SUPER_SPONSOR("");

	@Getter
	private final String color; //TODO for frontend

	Sponsorship(String color) {
		this.color = color;
	}

	public static Sponsorship getFromOrdinal(int ordinal){
		return Sponsorship.values()[ordinal];
	}
}
