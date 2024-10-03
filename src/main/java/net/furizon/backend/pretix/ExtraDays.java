package net.furizon.backend.pretix;

public enum ExtraDays implements PretixObject {
	NONE, EARLY, LATE;

	public static Sponsorship getFromOrdinal(int ordinal) {
		return Sponsorship.values()[ordinal];
	}
}
