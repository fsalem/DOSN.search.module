package dosn.search.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import dosn.utility.general.Helper;
import dosn.utility.json.UserJSON;

/**
 * 
 * This is a thread class to open multiple connections at the same time to the
 * potential servers
 * 
 */
public class PotentielServerConnectionThread extends Thread {

	private enum ConnectionType {
		REQUEST, RESPONSE
	}

	private String url = null;
	private List<String> interests = null;
	private String username = null;
	private List<String> potentielServers = null;
	private List<UserJSON> users = null;
	private UUID messageUID = null;
	private Integer maxHops = null;
	private PotentielServerConnectionResultListener resultListener = null;
	private ConnectionType connectionType = null;

	/**
	 * This constructor is to be called for REQUEST connection in case of
	 * searching by Interests
	 * 
	 * @param url
	 * @param interests
	 * @param maxHops
	 * @param resultListener
	 */
	public PotentielServerConnectionThread(String url, List<String> interests,
			Integer maxHops, UUID messageUID,
			PotentielServerConnectionResultListener resultListener) {
		super();
		this.url = url;
		this.interests = interests;
		this.username = null;
		this.maxHops = maxHops;
		this.messageUID = messageUID;
		this.connectionType = ConnectionType.REQUEST;
		this.resultListener = resultListener;
	}

	/**
	 * This constructor is to be called for REQUEST connection in case of
	 * searching by username
	 * 
	 * @param url
	 * @param interests
	 * @param maxHops
	 * @param resultListener
	 */
	public PotentielServerConnectionThread(String url, String username,
			Integer maxHops, UUID messageUID,
			PotentielServerConnectionResultListener resultListener) {
		super();
		this.url = url;
		this.interests = null;
		this.username = username;
		// this.username = null;
		this.maxHops = maxHops;
		this.messageUID = messageUID;
		this.connectionType = ConnectionType.REQUEST;
		this.resultListener = resultListener;
	}

	@Override
	public void run() {
		String json = sendRequestConnectionType();

		if (json != null) {
			resultListener.addJsonObject(json);
		}
		resultListener.addFinishedThread(getId());
	}

	private String sendRequestConnectionType() {
		if ((interests == null && username == null)
				|| (interests != null && username != null)) {
			System.out
					.println("hier wurde verpasst den Request weiter zu geben an der server:"
							+ url);
			return null;
		}
		String jsonRequest = null;
		if (interests != null) {
			jsonRequest = Helper.buildJSONRequest(interests, maxHops,
					messageUID);
		} else {
			jsonRequest = Helper
					.buildJSONRequest(username, maxHops, messageUID);
		}
		try {
			return connect(jsonRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out
				.println("hier wurde verpasst den Request weiter zu geben an der server:"
						+ url);
		return null;
	}

	/*
	 * private String sendResponseConnectionType() { String jsonResponse = null;
	 * if (this.searchMethod.equals(SearchMethod.BREADTH_SEARCH)) { jsonResponse
	 * = Helper.buildJSONResponse(this.users, this.potentielServers,
	 * this.messageUID); } else { jsonResponse = Helper
	 * .buildJSONResponse(this.users, this.messageUID); } try { return
	 * connect(jsonResponse); } catch (IOException e) { e.printStackTrace(); }
	 * return null; }
	 */

	private String connect(String json) throws IOException {
		try {
			URL urlPath = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlPath
					.openConnection();
			connection.setDoInput(Boolean.TRUE);
			connection.setDoOutput(Boolean.TRUE);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());
			out.write(json);
			out.close();

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			bufferedReader.close();
			System.out.println("succecfully connected to server: " + url);
			return stringBuilder.toString();

		} catch (Exception e) {
			System.out.println("connection timeout for server: " + url);
			return null;
		}
	}

}
