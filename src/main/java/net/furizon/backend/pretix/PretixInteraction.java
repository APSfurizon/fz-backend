package net.furizon.backend.pretix;

import net.furizon.backend.db.entities.pretix.Event;
import net.furizon.backend.utils.Download;
import net.furizon.backend.utils.ThrowableSupplier;
import net.furizon.backend.utils.Tuple;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Describes all the iteractions via Pretix
 */
public class PretixInteraction {

	private final static Logger LOGGER = LoggerFactory.getLogger(PretixInteraction.class);

	private enum PretixObjectBaseTypes implements PretixObject {
		ITEM_TICKET, ITEM_TICKET_DAILY, ITEM_MEMBERSHIP, ITEM_SPONSORSHIP, ITEM_EXTRA_DAYS, ITEM_ROOMS, QUESTION_SECRET
	}
	private class Room implements PretixObject{ public int capacity; }
	private class Daily implements PretixObject{ public int day; }

	//private static PretixIdsMap pretixIdsCache = null;
	private static Map<Integer, PretixObject> itemMap = new HashMap<>();
	private static Map<Integer, PretixObject> variationMap = new HashMap<>();
	private static Map<Integer, PretixObject> questionsMap = new HashMap<>();


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
		questionsMap.clear();
	}
	private static void reloadProducts() throws TimeoutException {
		itemMap.clear();
		variationMap.clear();
		getAllPages("items/", PretixSettings.generalSettings.getEventUrl(), (item) -> {
			JSONObject metadata = item.getJSONObject("meta_data");
			String identifier = item.getString(Constants.METADATA_IDENTIFIER_ITEM);
			int itemId = item.getInt("id");

			if(identifier.startsWith(Constants.METADATA_EXTRA_DAYS_TAG_PREFIX)){
				
			} else if(identifier.startsWith(Constants.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {

			} else switch(identifier){
				case Constants.METADATA_EVENT_TICKET: {
					itemMap.put(itemId, PretixObjectBaseTypes.ITEM_TICKET);
					break;
				}
				case Constants.METADATA_MEMBERSHIP_CARD: {
					itemMap.put(itemId, PretixObjectBaseTypes.ITEM_MEMBERSHIP);
					break;
				}
				case Constants.METADATA_SPONSORSHIP: {
					itemMap.put(itemId, PretixObjectBaseTypes.ITEM_SPONSORSHIP);
					break;
				}
				case Constants.METADATA_ROOM: {

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
