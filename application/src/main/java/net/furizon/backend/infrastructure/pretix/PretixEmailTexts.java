package net.furizon.backend.infrastructure.pretix;

import static net.furizon.backend.infrastructure.email.EmailVars.*;

public class PretixEmailTexts {
    public static final String SUBJECT_ORDER_PROBLEM = "There's a severe problem with your order!";


    public static final String TITLE_DUPLICATE_ORDER = "You made a duplicate order";


    public static final String BODY_DUPLICATE_ORDER = "You're receiving this email because we've found that you made two orders for the same event " + EVENT_NAME + "! "
            + "You can have only one active order at a time. Please communicate to our staff your main order code (" + ORDER_CODE + ") and your duplicate order code (" + DUPLICATE_ORDER_CODE + ") "
            + "to ask for help a possible refund.";

    public static final String LANG_PRETIX = "en";
}
