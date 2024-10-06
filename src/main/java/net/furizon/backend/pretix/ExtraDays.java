package net.furizon.backend.pretix;

public enum ExtraDays {
	NONE, EARLY, LATE, BOTH;

	public static Sponsorship getFromOrdinal(int ordinal) {
		return Sponsorship.values()[ordinal];
	}
}
