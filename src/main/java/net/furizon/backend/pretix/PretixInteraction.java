package net.furizon.backend.pretix;

import net.furizon.backend.db.entities.pretix.Event;
import net.furizon.backend.utils.Download;
import net.furizon.backend.utils.ThrowableSupplier;
import net.furizon.backend.utils.Tuple;
import org.apache.logging.log4j.util.TriConsumer;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Describes all the iteractions via Pretix
 */
public class PretixInteraction {

	private final static Logger LOGGER = LoggerFactory.getLogger(PretixInteraction.class);

	private static PretixIdsMap pretixIdsCache = null;
	private static class PretixIdsMap {

		public Set<Integer> ticketIds = new HashSet<>();
		public Map<Integer, Integer> dailyIds = new HashMap<>(); //map id -> day idx

		public Set<Integer> membershipCardIds = new HashSet<>();

		public Set<Integer> sponsorshipItemIds = new HashSet<>();
		public Map<Integer, Sponsorship> sponsorshipVariationIds = new HashMap<>();

		public Map<Integer, ExtraDays> extraDaysIds = new HashMap<>();

		public Set<Integer> roomItemIds = new HashSet<>();
		public Map<Integer, Tuple<Integer, String>> roomVariationIds = new HashMap<>(); //map id -> (capacity, hotelName)

		public int questionSecret = -1;

	}

	public static void reloadEverything(){
		reloadEvents();
		reloadOrders();
	}

	public static void reloadOrderData(){
		reloadProducts();
		reloadQuestions();
		reloadOrders();
	}

	private static void reloadOrders(){

	}
	private static void reloadQuestions(){

	}
	private static void reloadProducts() throws TimeoutException {
		pretixIdsCache = new PretixIdsMap();

		TriConsumer<JSONObject, String, BiConsumer<Integer, String>> searchVariations = (item, prefix, fnc) -> {
			JSONArray variations = item.getJSONArray("variations");
			for(int i = 0; i < variations.length(); i++){
				JSONObject variation = variations.getJSONObject(i);
				String identifierVariation = variation.getJSONObject("meta_data").getString(Constants.METADATA_IDENTIFIER_ITEM);
				int variationId = variation.getInt("id");

				if(identifierVariation.startsWith(prefix))
					fnc.accept(variationId, identifierVariation.substring(prefix.length()));
			}
		};

		getAllPages("items/", PretixSettings.generalSettings.getEventUrl(), (item) -> {
			String identifier = item.getJSONObject("meta_data").getString(Constants.METADATA_IDENTIFIER_ITEM);
			int itemId = item.getInt("id");

			if(identifier.startsWith(Constants.METADATA_EXTRA_DAYS_TAG_PREFIX)){
				String s = identifier.substring(Constants.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
				ExtraDays ed = ExtraDays.valueOf(s);
				pretixIdsCache.extraDaysIds.put(itemId, ed);


			} else if(identifier.startsWith(Constants.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
				String s = identifier.substring(Constants.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
				int day = Integer.parseInt(s);
				pretixIdsCache.dailyIds.put(itemId, day);

			} else switch(identifier){
				case Constants.METADATA_EVENT_TICKET: {
					pretixIdsCache.ticketIds.add(itemId);
					break;
				}
				case Constants.METADATA_MEMBERSHIP_CARD: {
					pretixIdsCache.membershipCardIds.add(itemId);
					break;
				}
				case Constants.METADATA_SPONSORSHIP: {
					pretixIdsCache.sponsorshipItemIds.add(itemId);
					searchVariations.accept(item, Constants.METADATA_SPONSORSHIP_VARIATIONS_TAG_PREFIX, (variationId, s) -> {
						Sponsorship ss = Sponsorship.valueOf(s);
						pretixIdsCache.sponsorshipVariationIds.put(variationId, ss);
					});
					break;
				}
				case Constants.METADATA_ROOM: {
					pretixIdsCache.roomItemIds.add(itemId);
					searchVariations.accept(item, Constants.METADATA_ROOM_TYPE_TAG_PREFIX, (variationId, s) -> {
						String[] sp = s.split("_");
						String hotelName = sp[0];
						int capacity = Integer.parseInt(sp[1]);
						pretixIdsCache.roomVariationIds.put(variationId, new Tuple<>(capacity, hotelName));
					});
					break;
				}
			}
		});
	}

	//TODO: Organizers and events can change slug and we have no other way to uniquely identify an event. Eventually: Create "something" which can move the events and related orders to a new one
	private static List<Tuple<String, String>> reloadOrganizers() throws TimeoutException {
		List<Tuple<String, String>> organizers = new LinkedList<>();
		getAllPages("organizers/", PretixSettings.generalSettings.getBaseUrl(), (res) -> organizers.add(new Tuple<>(res.getString("slug"), res.getString("public_url"))));
		return organizers;
	}
	public static void reloadEvents() throws TimeoutException {
		List<Tuple<String, String>> organizers = reloadOrganizers();

		//List<Event> ret = new LinkedList<>();
		String currentEvent = PretixSettings.generalSettings.getCurrentEvent();
		for(Tuple<String, String> o : organizers){
			String organizer = o.getA();
			getAllPages("organizers/" + organizer + "/events/", PretixSettings.generalSettings.getBaseUrl(), (res) -> {

				Map<String, String> names = new HashMap<>();
				JSONObject obj = res.getJSONObject("name");
				for(String s : obj.keySet()) names.put(s, obj.getString(s));

				//TODO check first if the order already exists, if not create a new object, if yes, obtain it and set the parameters. Then save it back to the db
				Event e = new Event(
							organizer,
							res.getString("slug"),
							o.getB(),
							names,
							res.getString("date_from"),
							res.getString("date_to")
						);
				e.setCurrentEvent(e.getSlug().equals(currentEvent));
				//ret.add(e);
			});
		}
		//return ret;
	}


	//Push orders to pretix
	public static void uploadToPretixDb(){
		//TODO: I will need to store the full raw questions of every order to reupload them (diocane)
	}

	private static void getAllPages(String url, String baseUrl, Consumer<JSONObject> elementFnc) throws TimeoutException {
		int[] expectedStatusCodes = {200, 404};

		int pages = 0;
		while(true){
			pages += 1;

			Download.Response res = doGet(url + "/?page=" + pages, baseUrl, expectedStatusCodes);
			if(res.getStatusCode() == 404) break;

			JSONObject response = res.getResponseJson();
			JSONArray objs = response.getJSONArray("results");
			for(int i = 0; i < objs.length(); i++)
				elementFnc.accept(objs.getJSONObject(i));

			if(response.getString("next") == null) break;
		}
	}

	private static Download httpClient;
	public static void updatePretixSettings(PretixSettings ps) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		httpClient = new Download(PretixSettings.connectionSettings.getTimeout(), PretixSettings.generalSettings.getConnectionHeaders(), null, PretixSettings.connectionSettings.getMaxHttpConnections(), false);
	}

	private static Download.Response doGet(String url, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.get(baseUrl + "/" + url).go(), null, expectedStatusCodes, "GETing");
	}
	private static Download.Response doPost(String url, Object content, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.post(baseUrl + "/" + url).setBody(content).go(), null, expectedStatusCodes, "POSTing");
	}
	private static Download.Response doPatch(String url, JSONObject json, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.patch(baseUrl + "/" + url).setJson(json).go(), null, expectedStatusCodes, "PATCHing");
	}

	private static Download.Response doRequest(String url, ThrowableSupplier<Download.Response, Exception> doReq, Runnable metricsFunc, int[] expectedStatusCodes, String opLogStr) throws TimeoutException {
		List<Integer> allowedStates = Arrays.stream(expectedStatusCodes).boxed().toList();
		Download.Response res = null;
		int maxRetries = PretixSettings.connectionSettings.getMaxRetries();
		for(int i = 0; i < maxRetries; i++){
			try {
				//metricsFunc.run(); TODO

				Download.Response r = doReq.get();

				int statusCode = r.getStatusCode();
				if(allowedStates != null && !allowedStates.contains(statusCode)){
					//incPretixErrors(); TODO
					LOGGER.warn("[PRETIX] Got an unexpected status code ({}) while {} '{}'. Allowed status codes: {}", statusCode, opLogStr, url, allowedStates);
					continue;
				}
				res = r;

			} catch (Exception e) {
				//incPretixErrors(); TODO
				LOGGER.warn("[PRETIX] An error ({}) occurred while {} '{}':\n{}", i, opLogStr, url, e.getMessage());
			}
		}
		if(res == null){
			LOGGER.error("[PRETIX] Reached PRETIX_REQUESTS_MAX ({}) while {} '{}'. Aborting", maxRetries, opLogStr, url);
			throw new TimeoutException("PRETIX_REQUESTS_MAX reached while " + opLogStr + " to pretix.");
		}
		return res;
	}
}
