package net.furizon.backend.utils.pretix;

public enum ExtraDays {
	NONE, EARLY, LATE, BOTH;

	public static Sponsorship getFromOrdinal(int ordinal) {
		return Sponsorship.values()[ordinal];
	}
}
