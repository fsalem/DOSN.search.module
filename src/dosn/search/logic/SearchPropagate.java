package dosn.search.logic;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import dosn.database.facade.DatabaseInteractionLayer;
import dosn.utility.general.Helper;
import dosn.utility.general.PropertiesLookup;
import dosn.utility.json.SRResponseJSON;
import dosn.utility.json.UserJSON;

/**
 * This class contains the logic of searching locally and propagate the request
 * to other servers. It contains different methods for searching by username or
 * interests
 */
@Scope(value = "prototype")
@Component
public class SearchPropagate {

	private List<String> allPotentielServers;

	@Inject
	DatabaseInteractionLayer databaseInteractionLayer;

	@Inject
	LocalSearch localSearch;

	@PostConstruct
	public void init() {
	}

	/**
	 * This method is to search by interests and propagate the request to the potential servers and return the json response
	 * 
	 * @param interests
	 * @param responsURI
	 * @param msgID
	 */
	public void searchAndPropagateByInterests(List<String> interests,
			String responsURI, UUID msgID) {
		List<UserJSON> users = localSearch.searchUsingInterest(interests);
		if (users != null && !users.isEmpty()) {
			sendCurrentResult(users, responsURI, msgID);
		}
		allPotentielServers = databaseInteractionLayer
				.retrievePotentielServers(PropertiesLookup.getMaxRequests());
		List<String> pendingPotentielServers = new ArrayList<String>(
				allPotentielServers);
		retreiveSetOfUsers(pendingPotentielServers, interests, null,
				PropertiesLookup.getSearchByInterestsUri(),
				PropertiesLookup.getMaxHops(), responsURI, msgID);

	}

	/**
	 * This method is to search by username and propagate the request to the potential servers and return the json response
	 * @param username
	 * @param responsURI
	 * @param msgID
	 */
	public void searchAndPropagateByUsername(String username,
			String responsURI, UUID msgID) {
		List<UserJSON> users = localSearch.searchUsingUsername(username);
		if (users != null && !users.isEmpty()) {
			sendCurrentResult(users, responsURI, msgID);
		}
		allPotentielServers = databaseInteractionLayer
				.retrievePotentielServers(PropertiesLookup.getMaxRequests());
		List<String> pendingPotentielServers = new ArrayList<String>(
				allPotentielServers);
		retreiveSetOfUsers(pendingPotentielServers, null, username,
				PropertiesLookup.getSearchByUsernameUri(),
				PropertiesLookup.getMaxHops(), responsURI, msgID);

	}

	/**
	 * 
	 * @param pendingPotentielServers
	 * @param interests
	 * @param username
	 * @param serviceURI
	 * @param maxHops
	 * @param responsURI
	 * @param msgID
	 */
	private void retreiveSetOfUsers(List<String> pendingPotentielServers,
			List<String> interests, String username, String serviceURI,
			Integer maxHops, String responsURI, UUID msgID) {
		if (maxHops == 0 || pendingPotentielServers == null
				|| pendingPotentielServers.isEmpty()
				|| (interests == null && username == null)) {
			System.out.println("finish: retreiveSetOfUser");
			return;
		}
		PotentielServerConnectionResultListener resultListener = new PotentielServerConnectionResultListener(
				pendingPotentielServers.size());
		List<Thread> threads = new ArrayList<Thread>();
		for (String server : pendingPotentielServers) {
			if (!server.endsWith("/"))
				server += "/";
			Thread thread = null;
			if (interests != null) {
				thread = new PotentielServerConnectionThread(server
						+ serviceURI, interests, maxHops, msgID, resultListener);
			} else {
				thread = new PotentielServerConnectionThread(server
						+ serviceURI, username, maxHops, msgID, resultListener);
			}
			thread.start();
			threads.add(thread);
		}
		while (threads.size() != resultListener.getFinishedThreads().size()) {
			synchronized (resultListener) {
				try {
					resultListener.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("all threads send search result");
		pendingPotentielServers = new ArrayList<String>();
		List<UserJSON> users = new ArrayList<UserJSON>();
		for (String json : resultListener.getJsonObjects()) {
			SRResponseJSON responseJSON = Helper.getJSONResponse(json);
			if (responseJSON.getUsers() != null) {
				users.addAll(responseJSON.getUsers());
			}
			if (responseJSON.getPotentielServers() != null
					&& !responseJSON.getPotentielServers().isEmpty()) {
				databaseInteractionLayer
						.addNonExistingPotentielServers(responseJSON
								.getPotentielServers());
				for (String pServer : responseJSON.getPotentielServers()) {
					if (!allPotentielServers.contains(pServer)) {
						allPotentielServers.add(pServer);
						pendingPotentielServers.add(pServer);
					}
				}
			}
		}
		sendCurrentResult(users, responsURI, msgID);
		retreiveSetOfUsers(pendingPotentielServers, interests, username,
				serviceURI, --maxHops, responsURI, msgID);
	}

	/**
	 * This method is to send the result to the requester
	 * @param users
	 * @param responsURI
	 * @param msgID
	 */
	private void sendCurrentResult(List<UserJSON> users, String responsURI,
			UUID msgID) {
		System.out.println("sendCurrentResults to:" + responsURI
				+ "  with msgID: " + msgID);
		try {

			URL url = new URL(responsURI);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());
			out.write(Helper.buildJSONResponse(users, msgID.toString()));
			out.close();
			connection.connect();
			connection.getResponseCode();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
