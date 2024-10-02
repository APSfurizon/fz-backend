package net.furizon.backend.pretix;

import net.furizon.backend.utils.Download;
import net.furizon.backend.utils.ThrowableSupplier;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Describes all the iteractions via Pretix
 */
public class PretixInteraction {

	public static void reloadEverything(){
		reloadEventData();
		reloadOrders();
	}

	public static void reloadEventData(){
		reloadOrganizers();
		reloadEvents();
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
	private static void reloadProducts(){

	}

	private static void reloadOrganizers(){

	}
	private static void reloadEvents(){

	}


	//Push orders to pretix
	public static void uploadToPretixDb(){

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
		for(int i = 0; i < PretixSettings.connectionSettings.getMaxRetries(); i++){
			try {
				//metricsFunc.run(); TODO

				Download.Response r = doReq.get();

				if(allowedStates != null && !allowedStates.contains(r.getResponseCode())){
					//incPretixErrors(); TODO
					//TODO log error
					continue;
				}
				res = r;

			} catch (Exception e) {
				//incPretixErrors(); TODO
				//TODO log error
			}
		}
		if(res == null){
			//TODO log max retries error
			throw new TimeoutException("PRETIX_REQUESTS_MAX reached while " + opLogStr + " to pretix.");
		}
		return res;
	}
}
