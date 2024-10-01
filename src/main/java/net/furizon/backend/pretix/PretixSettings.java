package net.furizon.backend.pretix;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public final class PretixSettings {
	public GeneralSettings generalSettings = new GeneralSettings();
	public PropicSettings propicSettings = new PropicSettings();

	public final class GeneralSettings {
		@Getter
		private String apiKey;

		@Getter
		private String organizer, event;
		@Getter
		private String hostName;

		@Getter @Setter
		private boolean runHealtcheck;


		@Getter
		private Map<String, String> connectionHeaders = new HashMap<String, String>();
		@Getter
		private String baseUrl = null;
		@Getter
		private String eventUrl = null;


		public void setHostName(String hostName) {
			this.hostName = hostName;
			connectionHeaders.put("Host", hostName);
		}
		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
			connectionHeaders.put("Authorization", "Token " + apiKey);
		}

		public void setEndpointUrl(String endpointUrl) {
			if(!endpointUrl.endsWith("/")) endpointUrl += "/";
			baseUrl = endpointUrl + "api/v1/";
			generateEventUrl();
		}
		public void setOrganizer(String organizer) {
			this.organizer = organizer;
			generateEventUrl();
		}
		public void setEvent(String event) {
			this.event = event;
			generateEventUrl();
		}

		private void generateEventUrl(){
			eventUrl = getBaseUrl() + "organizers/" + organizer + "/events/" + event + "/";
		}
	}
	
	public final class PropicSettings {
		@Getter @Setter
		private long deadlineTs;
		@Getter @Setter
		private long maxFileSizeBytes;
		@Getter @Setter
		private long maxSizeX, maxSizeY;
		@Getter @Setter
		private long minSizeX, minSizeY;
	}
}
