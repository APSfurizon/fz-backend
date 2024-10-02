package net.furizon.backend.pretix;

import net.furizon.backend.db.entities.pretix.settings.PretixConnectionSettings;
import net.furizon.backend.db.entities.pretix.settings.PretixGeneralSettings;
import net.furizon.backend.db.entities.pretix.settings.PretixPropicSettings;

public class PretixSettings {

	public static final PretixConnectionSettings connectionSettings = new PretixConnectionSettings();
	public static final PretixGeneralSettings generalSettings = new PretixGeneralSettings();
	public static final PretixPropicSettings propicSettings = new PretixPropicSettings();

}
