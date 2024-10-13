package net.furizon.backend.pretix;

import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.db.entities.pretix.Event;
import net.furizon.backend.db.entities.pretix.Order;
import net.furizon.backend.db.entities.users.User;
import net.furizon.backend.utils.Download;
import net.furizon.backend.utils.ThrowableSupplier;
import org.apache.logging.log4j.util.TriConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.util.Pair;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Describes all the iteractions via Pretix
 */
@Slf4j
public class PretixInteraction {
    private static PretixIdsMap pretixIdsCache = null;

    private static class PretixIdsMap {

        public Set<Integer> ticketIds = new HashSet<>();
        public Map<Integer, Integer> dailyIds = new HashMap<>(); //map id -> day idx

        public Set<Integer> membershipCardIds = new HashSet<>();

        public Set<Integer> sponsorshipItemIds = new HashSet<>();
        public Map<Integer, Sponsorship> sponsorshipVariationIds = new HashMap<>();

        public Map<Integer, ExtraDays> extraDaysIds = new HashMap<>();

        public Set<Integer> roomItemIds = new HashSet<>();
        public Map<Integer, Pair<Integer, String>> roomVariationIds = new HashMap<>(); //map id -> (capacity, hotelName)

        public Map<Integer, QuestionType> questionTypeIds = new HashMap<>();
        public Map<Integer, String> questionIdentifiers = new HashMap<>();
        public Map<String, Integer> questionIdentifiersToId = new HashMap<>();

        public int questionSecret = -1;

    }

    public static boolean reloadEverything() {
        try {
            reloadEvents();
            reloadOrders();
            return true;
        } catch (TimeoutException te) {
            return false;
        }
    }

    public static boolean reloadOrderData() {
        try {
            PretixIdsMap cache = new PretixIdsMap();
            reloadProducts(cache);
            reloadQuestions(cache);
            pretixIdsCache = cache;
            reloadOrders();
            return true;
        } catch (TimeoutException te) {
            return false;
        }
    }

    private static void reloadOrders() throws TimeoutException {
        getAllPages(
            "orders/",
            PretixSettings.generalSettings.getEventUrl(),
            PretixInteraction::parseOrderAndUpdateDatabase
        );
    }

    private static void reloadQuestions(PretixIdsMap pretixIdsCache) throws TimeoutException {
        getAllPages("questions/", PretixSettings.generalSettings.getEventUrl(), (item) -> {
            int id = item.getInt("id");
            String identifier = item.getString("identifier");
            pretixIdsCache.questionTypeIds.put(id, QuestionType.fromCode(item.getString("type")));
            pretixIdsCache.questionIdentifiers.put(id, identifier);
            pretixIdsCache.questionIdentifiersToId.put(identifier, id);

            if (item.getString("identifier").equals(Constants.QUESTIONS_ACCOUNT_SECRET)) {
                pretixIdsCache.questionSecret = id;
            }
        });
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    private static void reloadProducts(PretixIdsMap pretixIdsCache) throws TimeoutException {
        TriConsumer<JSONObject, String, BiConsumer<Integer, String>> searchVariations = (item, prefix, fnc) -> {
            JSONArray variations = item.getJSONArray("variations");
            for (int i = 0; i < variations.length(); i++) {
                JSONObject variation = variations.getJSONObject(i);
                String identifierVariation = variation
                    .getJSONObject("meta_data")
                    .getString(Constants.METADATA_IDENTIFIER_ITEM);
                int variationId = variation.getInt("id");

                if (identifierVariation.startsWith(prefix)) {
                    fnc.accept(variationId, identifierVariation.substring(prefix.length()));
                }
            }
        };

        getAllPages("items/", PretixSettings.generalSettings.getEventUrl(), (item) -> {
            String identifier = item.getJSONObject("meta_data").getString(Constants.METADATA_IDENTIFIER_ITEM);
            int itemId = item.getInt("id");

            if (identifier.startsWith(Constants.METADATA_EXTRA_DAYS_TAG_PREFIX)) {
                String s = identifier.substring(Constants.METADATA_EXTRA_DAYS_TAG_PREFIX.length());
                ExtraDays ed = ExtraDays.valueOf(s);
                pretixIdsCache.extraDaysIds.put(itemId, ed);


            } else if (identifier.startsWith(Constants.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX)) {
                String s = identifier.substring(Constants.METADATA_EVENT_TICKET_DAILY_TAG_PREFIX.length());
                int day = Integer.parseInt(s);
                pretixIdsCache.dailyIds.put(itemId, day);

            } else {
                switch (identifier) {
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
                        searchVariations.accept(
                            item,
                            Constants.METADATA_SPONSORSHIP_VARIATIONS_TAG_PREFIX,
                            (variationId, s) -> {
                                Sponsorship ss = Sponsorship.valueOf(s);
                                pretixIdsCache.sponsorshipVariationIds.put(variationId, ss);
                            }
                        );
                        break;
                    }
                    case Constants.METADATA_ROOM: {
                        pretixIdsCache.roomItemIds.add(itemId);
                        searchVariations.accept(item, Constants.METADATA_ROOM_TYPE_TAG_PREFIX, (variationId, s) -> {
                            String[] sp = s.split("_");
                            String hotelName = sp[0];
                            int capacity = Integer.parseInt(sp[1]);
                            pretixIdsCache.roomVariationIds.put(variationId, Pair.of(capacity, hotelName));
                        });
                        break;
                    }
                    default:
                        break; // TODO -> Do we care about default here?
                }
            }
        });
    }

    public static void fetchOrder(String code, String secret) throws TimeoutException {
        Download.Response res = doGet(
            "orders/" + code.replaceAll("[^A-Za-z0-9]+", ""),
            PretixSettings.generalSettings.getEventUrl(),
            Constants.STATUS_CODES_WITH_404
        );
        if (res.getStatusCode() == 404) {
            throw new RuntimeException("Order not found");
        }
        JSONObject orderData = res.getResponseJson();
        if (!orderData.getString("secret").equals(secret)) {
            throw new RuntimeException("Order not found"); //Same exception to not leak matched order code
        }
        parseOrderAndUpdateDatabase(orderData);
    }

    private static void parseOrderAndUpdateDatabase(JSONObject orderData) {
        PretixIdsMap pretixCache = pretixIdsCache; // why?
        boolean hasTicket = false; //If no ticket is found, we don't store the order at all

        String code = orderData.getString("coded");
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
        for (int i = 0; i < positions.length(); i++) {
            JSONObject position = positions.getJSONObject(i);
            int item = position.getInt("item");

            if (pretixCache.ticketIds.contains(item)) {
                hasTicket = true;
                answersMainPositionId = position.getInt("id");
                answers = position.getJSONArray("answers");
                for (int j = 0; j < answers.length(); j++) {
                    JSONObject answer = answers.getJSONObject(j);
                    int questionId = answer.getInt("question");
                    if (translateQuestionType(questionId) == QuestionType.FILE) {
                        //TODO: check if changes are reflected in original array (they should)
                        answer.put("answer", Constants.QUESTIONS_FILE_KEEP);
                    }

                    if (questionId == pretixCache.questionSecret) {
                        userSecret = answer.getString("answer");
                    }
                }
            } else if (pretixCache.dailyIds.containsKey(item)) {
                days.add(pretixCache.dailyIds.get(item));
            } else if (pretixCache.membershipCardIds.contains(item)) {
                membership = true;
            } else if (pretixCache.sponsorshipItemIds.contains(item)) {
                Sponsorship s = pretixCache.sponsorshipVariationIds.get(position.getInt("variation"));
                if (s.ordinal() > sponsorship.ordinal()) {
                    sponsorship = s; //keep the best sponsorship
                }
            } else if (pretixCache.extraDaysIds.containsKey(item)) {
                ExtraDays d = pretixCache.extraDaysIds.get(item);
                if (extraDays != ExtraDays.BOTH) {
                    if (extraDays != d && extraDays != ExtraDays.NONE) {
                        extraDays = ExtraDays.BOTH;
                    } else {
                        extraDays = d;
                    }
                }
            } else if (pretixCache.roomItemIds.contains(item)) {
                Pair<Integer, String> room = pretixCache.roomVariationIds.get(position.getInt("variation"));
                roomCapacity = room.getFirst();
                hotelLocation = room.getSecond();
            }
        }

        if (hasTicket) {
            //TODO: fetch user from db by userSecret
            User u = null;

            Order order = null; //TODO: try fetching from db
            //order not found
            if (order == null) {
                order = new Order();
            }

            order.update(
                code,
                secret,
                answersMainPositionId,
                days,
                sponsorship,
                extraDays,
                roomCapacity,
                hotelLocation,
                membership,
                u,
                PretixSettings.generalSettings.getCurrentEventObj(),
                answers
            );
        } else {
            //TODO: delete order
        }
    }


    //TODO: Organizers and events can change slug and we have no other way to uniquely identify an event.
    // Eventually: Create "something" which can move the events and related orders to a new one
    private static List<Pair<String, String>> reloadOrganizers() throws TimeoutException {
        List<Pair<String, String>> organizers = new LinkedList<>();
        getAllPages(
            "organizers/",
            PretixSettings.generalSettings.getBaseUrl(),
            (res) -> organizers.add(
                Pair.of(res.getString("slug"), res.getString("public_url"))
            )
        );
        return organizers;
    }

    public static void reloadEvents() throws TimeoutException {
        List<Pair<String, String>> organizers = reloadOrganizers();

        //List<Event> ret = new LinkedList<>();
        String currentEvent = PretixSettings.generalSettings.getCurrentEvent();
        for (Pair<String, String> o : organizers) {
            String organizer = o.getFirst();
            getAllPages("organizers/" + organizer + "/events/", PretixSettings.generalSettings.getBaseUrl(), (res) -> {

                Map<String, String> names = new HashMap<>();
                JSONObject obj = res.getJSONObject("name");
                for (String s : obj.keySet()) {
                    names.put(s, obj.getString(s));
                }

                //TODO: check first if the order already exists,
                // if not create a new object, if yes, obtain it and set the parameters. Then save it back to the db
                Event e = new Event(
                    organizer,
                    res.getString("slug"),
                    o.getSecond(),
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


    public static String translateQuestionId(int answerId) {
        return pretixIdsCache.questionIdentifiers.get(answerId);
    }

    public static QuestionType translateQuestionType(int answerId) {
        return pretixIdsCache.questionTypeIds.get(answerId);
    }

    public static QuestionType translateQuestionType(String questionIdentifier) {
        return pretixIdsCache.questionTypeIds.get(pretixIdsCache.questionIdentifiersToId.get(questionIdentifier));
    }

    //Push orders to pretix
    public static void uploadToPretixDb() {
        //TODO: I will need to store the full raw questions of every order to reupload them (diocane)
    }

    private static void getAllPages(
        String url,
        String baseUrl,
        Consumer<JSONObject> elementFnc
    ) throws TimeoutException {
        int pages = 0;
        while (true) {
            pages += 1;

            Download.Response res = doGet(url + "/?page=" + pages, baseUrl, Constants.STATUS_CODES_WITH_404);
            if (res.getStatusCode() == 404) {
                break;
            }

            JSONObject response = res.getResponseJson();
            JSONArray objs = response.getJSONArray("results");
            for (int i = 0; i < objs.length(); i++) {
                elementFnc.accept(objs.getJSONObject(i));
            }

            if (response.getString("next") == null) {
                break;
            }
        }
    }

    private static Download httpClient;

    // Are you sure if is it okay? Why not use Spring Configuration class?
    public static void updatePretixSettings(
        PretixSettings ps
    ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        httpClient = new Download(
            PretixSettings.connectionSettings.getTimeout(),
            PretixSettings.generalSettings.getConnectionHeaders(),
            null,
            PretixSettings.connectionSettings.getMaxHttpConnections(),
            false
        );
    }

    private static Download.Response doGet(
        String url,
        String baseUrl,
        int[] expectedStatusCodes
    ) throws TimeoutException {
        return doRequest(
            url,
            () -> httpClient.get(baseUrl + "/" + url).go(),
            null,
            expectedStatusCodes,
            "GETing"
        );
    }

    private static Download.Response doPost(
        String url,
        Object content,
        String baseUrl,
        int[] expectedStatusCodes
    ) throws TimeoutException {
        return doRequest(
            url,
            () -> httpClient.post(baseUrl + "/" + url).setBody(content).go(),
            null,
            expectedStatusCodes,
            "POSTing"
        );
    }

    private static Download.Response doPatch(
        String url,
        JSONObject json,
        String baseUrl,
        int[] expectedStatusCodes
    ) throws TimeoutException {
        return doRequest(
            url,
            () -> httpClient.patch(baseUrl + "/" + url).setJson(json).go(),
            null,
            expectedStatusCodes,
            "PATCHing"
        );
    }

    private static Download.Response doRequest(
        String url,
        ThrowableSupplier<Download.Response, Exception> doReq,
        Runnable metricsFunc,
        int[] expectedStatusCodes,
        String opLogStr
    ) throws TimeoutException {
        List<Integer> allowedStates = Arrays.stream(expectedStatusCodes).boxed().toList();
        Download.Response res = null;
        int maxRetries = PretixSettings.connectionSettings.getMaxRetries();
        for (int i = 0; i < maxRetries; i++) {
            try {
                //metricsFunc.run(); TODO

                Download.Response r = doReq.get();

                int statusCode = r.getStatusCode();
                if (allowedStates != null && !allowedStates.contains(statusCode)) {
                    //incPretixErrors(); TODO
                    log.warn(
                        "[PRETIX] Got an unexpected status code ({}) while {} '{}'. Allowed status codes: {}",
                        statusCode,
                        opLogStr,
                        url,
                        allowedStates
                    );
                    continue;
                }
                res = r;

            } catch (Exception e) {
                //incPretixErrors(); TODO
                log.warn("[PRETIX] An error ({}) occurred while {} '{}':\n{}", i, opLogStr, url, e.getMessage());
            }
        }
        if (res == null) {
            log.error("[PRETIX] Reached PRETIX_REQUESTS_MAX ({}) while {} '{}'. Aborting", maxRetries, opLogStr, url);
            throw new TimeoutException("PRETIX_REQUESTS_MAX reached while " + opLogStr + " to pretix.");
        }
        return res;
    }
}
