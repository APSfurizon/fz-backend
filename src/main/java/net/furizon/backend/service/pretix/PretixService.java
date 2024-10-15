package net.furizon.backend.service.pretix;

import net.furizon.backend.db.entities.pretix.Event;
import net.furizon.backend.db.entities.pretix.Order;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.db.repositories.pretix.IEventRepository;
import net.furizon.backend.db.repositories.pretix.IOrderRepository;
import net.furizon.backend.db.repositories.users.IUserRepository;
import net.furizon.backend.utils.TextUtil;
import net.furizon.backend.utils.pretix.Constants;
import net.furizon.backend.utils.pretix.ExtraDays;
import net.furizon.backend.utils.pretix.QuestionType;
import net.furizon.backend.utils.pretix.Sponsorship;
import net.furizon.backend.utils.Download;
import net.furizon.backend.utils.ThrowableSupplier;
import net.furizon.backend.utils.Tuple;
import net.furizon.backend.utils.configs.PretixConfig;
import org.apache.logging.log4j.util.TriConsumer;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class PretixService {
	private final IUserRepository userRepository;
	private final IEventRepository eventRepository;
	private final IOrderRepository orderRepository;
	private PretixConfig pretixConfig;

	@Autowired
	private PretixService (IUserRepository userRepository, IEventRepository eventRepository, IOrderRepository orderRepository, PretixConfig pretixConfig) {
		this.userRepository = userRepository;
		this.eventRepository = eventRepository;
		this.orderRepository = orderRepository;
		this.pretixConfig = pretixConfig;
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(PretixService.class);

	private PretixIdsMap pretixIdsCache = null;
	private class PretixIdsMap {

		public Set<Integer> ticketIds = new HashSet<>();
		public Map<Integer, Integer> dailyIds = new HashMap<>(); //map id -> day idx

		public Set<Integer> membershipCardIds = new HashSet<>();

		public Set<Integer> sponsorshipItemIds = new HashSet<>();
		public Map<Integer, Sponsorship> sponsorshipVariationIds = new HashMap<>();

		public Map<Integer, ExtraDays> extraDaysIds = new HashMap<>();

		public Set<Integer> roomItemIds = new HashSet<>();
		public Map<Integer, Tuple<Integer, String>> roomVariationIds = new HashMap<>(); //map id -> (capacity, hotelName)

		public Map<Integer, QuestionType> questionTypeIds = new HashMap<>();
		public Map<Integer, String> questionIdentifiers = new HashMap<>();
		public Map<String, Integer> questionIdentifiersToId = new HashMap<>();

		public int questionSecret = -1;

	}

	public boolean reloadEverything(){
		try {
			reloadEvents();
			reloadOrderData();
			return true;
		} catch(TimeoutException te) {
			return false;
		}
	}

	public boolean reloadOrderData(){
		try {
			PretixIdsMap cache = new PretixIdsMap();
			reloadProducts(cache);
			reloadQuestions(cache);
			pretixIdsCache = cache;
			reloadOrders();
			return true;
		} catch(TimeoutException te) {
			return false;
		}
	}

	private synchronized void reloadOrders() throws TimeoutException {
		getAllPages("orders", pretixConfig.getEventUrl(), this::parseOrderAndUpdateDB);
	}

	private synchronized void reloadQuestions(PretixIdsMap pretixIdsCache) throws TimeoutException {
		getAllPages("questions", pretixConfig.getEventUrl(), (item) -> {
			int id = item.getInt("id");
			String identifier = item.getString("identifier");
			pretixIdsCache.questionTypeIds.put(id, QuestionType.fromCode(item.getString("type")));
			pretixIdsCache.questionIdentifiers.put(id, identifier);
			pretixIdsCache.questionIdentifiersToId.put(identifier, id);

			if(item.getString("identifier").equals(Constants.QUESTIONS_ACCOUNT_SECRET))
				pretixIdsCache.questionSecret = id;
		});
	}
	private synchronized void reloadProducts(PretixIdsMap pretixIdsCache) throws TimeoutException {
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

		getAllPages("items", pretixConfig.getEventUrl(), (item) -> {
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

	public void fetchOrder(String code, String secret) throws TimeoutException {
		Download.Response res = doGet("orders" + code.replaceAll("[^A-Za-z0-9]+", ""), pretixConfig.getEventUrl(), Constants.STATUS_CODES_WITH_404);
		if(res.getStatusCode() == 404) throw new RuntimeException("Order not found");
		JSONObject orderData = res.getResponseJson();
		if(!orderData.getString("secret").equals(secret)) throw new RuntimeException("Order not found"); //Same exception to not leak matched order code
		parseOrderAndUpdateDB(orderData);
	}
	private void parseOrderAndUpdateDB(JSONObject orderData){
		PretixIdsMap pCache = pretixIdsCache;
		boolean hasTicket = false; //If no ticket is found, we don't store the order at all

		String code = orderData.getString("code");
		String secret = orderData.getString("secret");

		Set<Integer> days = new HashSet<>();
		Sponsorship sponsorship = Sponsorship.NONE;
		ExtraDays extraDays = ExtraDays.NONE;
		int answersMainPositionId = 0;
		String hotelLocation = null;
		boolean membership = false;
		JSONArray answers = null;
		String userSecret = null;
		int roomCapacity = 0;

		JSONArray positions = orderData.getJSONArray("positions");
		for(int i = 0; i < positions.length(); i++){
			JSONObject position = positions.getJSONObject(i);
			int item = position.getInt("item");

			if(pCache.ticketIds.contains(item)){
				hasTicket = true;
				answersMainPositionId = position.getInt("id");
				answers = position.getJSONArray("answers");
				for(int j = 0; j < answers.length(); j++){
					JSONObject answer = answers.getJSONObject(j);
					int qId = answer.getInt("question");
					if(translateQuestionType(qId) == QuestionType.FILE)
						answer.put("answer", Constants.QUESTIONS_FILE_KEEP); //TODO: check if changes are reflected in original array (they should)
					if(qId == pCache.questionSecret)
						userSecret = answer.getString("answer");
				}
			} else

			if(pCache.dailyIds.containsKey(item))
				days.add(pCache.dailyIds.get(item));
			else

			if(pCache.membershipCardIds.contains(item))
				membership = true;
			else

			if(pCache.sponsorshipItemIds.contains(item)) {
				Sponsorship s = pCache.sponsorshipVariationIds.get(position.getInt("variation"));
				if(s.ordinal() > sponsorship.ordinal()) sponsorship = s; //keep the best sponsorship
			} else

			if(pCache.extraDaysIds.containsKey(item)) {
				ExtraDays d = pCache.extraDaysIds.get(item);
				if(extraDays != ExtraDays.BOTH) {
					if (extraDays != d && extraDays != ExtraDays.NONE) {
						extraDays = ExtraDays.BOTH;
					} else extraDays = d;
				}
			} else

			if(pCache.roomItemIds.contains(item)) {
				Tuple<Integer, String> room = pCache.roomVariationIds.get(position.getInt("variation"));
				roomCapacity = room.getA();
				hotelLocation = room.getB();
			}
		}

		// Fetch Order by code
		Order order = orderRepository.findByCodeAndEvent(code, pretixConfig.getCurrentEventObj().getSlug()).orElse(null);
		if(hasTicket) {
			// fetch user from db by userSecret
			User usr = null;
			if (!TextUtil.isEmpty(userSecret))
				usr = userRepository.findBySecret(userSecret).orElse(null);

			if (order == null) //order not found
				order = new Order();
			order.update(code, secret, answersMainPositionId, days, sponsorship, extraDays, roomCapacity, hotelLocation, membership, usr, pretixConfig.getCurrentEventObj(), answers);
			orderRepository.save(order);
		} else {
			if(order != null)
				orderRepository.delete(order);
		}
	}

	//TODO: Organizers and events can change slug and we have no other way to uniquely identify an event. Eventually: Create "something" which can move the events and related orders to a new one
	private List<Tuple<String, String>> reloadOrganizers() throws TimeoutException {
		List<Tuple<String, String>> organizers = new LinkedList<>();
		getAllPages("organizers/", pretixConfig.getBaseUrl(), (res) -> organizers.add(new Tuple<>(res.getString("slug"), res.getString("public_url"))));
		return organizers;
	}

	public void reloadEvents() throws TimeoutException {
		List<Tuple<String, String>> organizers = reloadOrganizers();

		//List<Event> ret = new LinkedList<>();
		String currentEvent = pretixConfig.getCurrentEvent();
		String currentOrg = pretixConfig.getOrganizer();
		for(Tuple<String, String> organizerTuple : organizers){
			String organizer = organizerTuple.getA();
			getAllPages(TextUtil.url("organizers", organizer, "events"), pretixConfig.getBaseUrl(), (res) -> {

				Map<String, String> names = new HashMap<>();
				JSONObject obj = res.getJSONObject("name");
				for(String s : obj.keySet()) names.put(s, obj.getString(s));

				String eventCode = res.getString("slug");
				Event evt = eventRepository.findById(Event.getSlug(organizer, eventCode)).orElse(null);
				if (evt == null) {
					evt = new Event(
							organizer,
							res.getString("slug"),
							organizerTuple.getB(),
							names,
							res.getString("date_from"),
							res.getString("date_to")
					);
					evt.setCurrentEvent(evt.getSlug().equals(Event.getSlug(currentOrg, currentEvent)));
					evt = eventRepository.save(evt);
				}
				if (evt != null && evt.isCurrentEvent()) {
					pretixConfig.setCurrentEventObj(evt);
				}
			});
		}
		//return ret;
	}


	public synchronized String translateQuestionId(int answerId){
		return pretixIdsCache.questionIdentifiers.get(answerId);
	}
	public synchronized QuestionType translateQuestionType(int answerId){
		return pretixIdsCache.questionTypeIds.get(answerId);
	}
	public synchronized QuestionType translateQuestionType(String questionIdentifier){
		return pretixIdsCache.questionTypeIds.get(pretixIdsCache.questionIdentifiersToId.get(questionIdentifier));
	}

	//Push orders to pretix
	public synchronized void uploadToPretixDb(){
		//TODO: I will need to store the full raw questions of every order to reupload them (diocane)
	}

	private void getAllPages(String url, String baseUrl, Consumer<JSONObject> elementFnc) throws TimeoutException {
		int pages = 0;
		while(true){
			pages += 1;

			Download.Response res = doGet(TextUtil.leadingSlash(url) + "?page=" + pages, baseUrl, Constants.STATUS_CODES_WITH_404);
			if(res.getStatusCode() == 404) break;

			JSONObject response = res.getResponseJson();
			JSONArray objs = response.getJSONArray("results");
			for(int i = 0; i < objs.length(); i++)
				elementFnc.accept(objs.getJSONObject(i));

			if(response.get("next") == null) break;
		}
	}

	private static Download httpClient;

	public void setupClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		httpClient = new Download(pretixConfig.getConnectionTimeout(), pretixConfig.getConnectionHeaders(), null, pretixConfig.getMaxConnections(), false);
	}

	private Download.Response doGet(String url, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.get(TextUtil.leadingSlash(baseUrl) + url).go(), null, expectedStatusCodes, "GETing");
	}

	private Download.Response doPost(String url, Object content, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.post(TextUtil.leadingSlash(baseUrl) + url).setBody(content).go(), null, expectedStatusCodes, "POSTing");
	}

	private Download.Response doPatch(String url, JSONObject json, String baseUrl, int[] expectedStatusCodes) throws TimeoutException {
		return doRequest(url, () -> httpClient.patch(TextUtil.leadingSlash(baseUrl) + url).setJson(json).go(), null, expectedStatusCodes, "PATCHing");
	}

	private Download.Response doRequest(String url, ThrowableSupplier<Download.Response, Exception> doReq, Runnable metricsFunc, int[] expectedStatusCodes, String opLogStr) throws TimeoutException {
		List<Integer> allowedStates = Arrays.stream(expectedStatusCodes).boxed().toList();
		Download.Response res = null;
		int maxRetries = pretixConfig.getMaxConnectionRetries();
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
			if (res != null) break;
		}
		if(res == null){
			LOGGER.error("[PRETIX] Reached PRETIX_REQUESTS_MAX ({}) while {} '{}'. Aborting", maxRetries, opLogStr, url);
			throw new TimeoutException("PRETIX_REQUESTS_MAX reached while " + opLogStr + " to pretix.");
		}
		return res;
	}
}
