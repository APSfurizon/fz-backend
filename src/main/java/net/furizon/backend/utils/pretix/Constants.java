package net.furizon.backend.utils.pretix;

public class Constants {
	public static final String METADATA_IDENTIFIER_ITEM = "item_name";
	//public static final String METADATA_CATEGORY_IDENTIFIER = "category_name";

	//public static final String CATEGORY_TICKETS = "tickets";
	//public static final String CATEGORY_MEMBERSHIPS = "memberships";
	//public static final String CATEGORY_SPONSORSHIPS = "sponsorships";


	public static final String METADATA_EVENT_TICKET = "ticket";
	public static final String METADATA_EVENT_TICKET_DAILY_TAG_PREFIX = "ticket_daily_";

	public static final String METADATA_MEMBERSHIP_CARD = "membership_card";

	public static final String METADATA_SPONSORSHIP = "sponsorship";
	public static final String METADATA_SPONSORSHIP_VARIATIONS_TAG_PREFIX = "sponsorship_type_";

	public static final String METADATA_EXTRA_DAYS_TAG_PREFIX = "extra_days_";

	public static final String METADATA_ROOM = "room";
	public static final String METADATA_ROOM_TYPE_TAG_PREFIX = "room_type_";

	public static final String QUESTIONS_ACCOUNT_SECRET = "account_secret";

	public static final String QUESTIONS_FILE_KEEP = "file:keep";

	// Why? you have Spring Http Codes Class :)
	public static final int[] STATUS_CODES_WITH_404 = new int[] { 200, 404 };
	public static final int[] STATUS_CODES_ONLY_200 = new int[] { 200 };
}
