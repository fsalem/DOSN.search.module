package dosn.search.logic;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.stereotype.Component;

import dosn.database.entities.User;
import dosn.database.facade.DatabaseInteractionLayer;
import dosn.utility.general.Helper;
import dosn.utility.general.PropertiesLookup;
import dosn.utility.json.UserJSON;
import dosn.utility.userIdentifier.impl.SONICUserIdentifier;
import dosn.utility.userIdentifier.impl.URIUserIdentifier;

/**
 * This class contains the logic of searching locally. It contains different
 * methods for searching by username or interests
 */
@Singleton
@Component
public class LocalSearch {

	@Inject
	DatabaseInteractionLayer databaseInteractionLayer;

	@Inject
	SONICUserIdentifier sonicUserIdentifier;

	@Inject
	URIUserIdentifier uriUserIdentifier;

	/**
	 * This method is to search by interests and return the json response
	 * 
	 * @param interests
	 * @param messageUID
	 * @return json response string
	 */
	public String searchUsingInterest(List<String> interests, String messageUID) {
		List<User> users = databaseInteractionLayer
				.retrieveUsersByInterest(interests);
		List<String> potentielServers = databaseInteractionLayer
				.retrievePotentielServers();
		List<UserJSON> userJSONs = convertToUserJSON(users);
		return Helper
				.buildJSONResponse(userJSONs, potentielServers, messageUID);
	}

	/**
	 * This method is to search by interests and return list of users only
	 * 
	 * @param interests
	 * @return list of users
	 */
	public List<UserJSON> searchUsingInterest(List<String> interests) {
		List<User> users = databaseInteractionLayer
				.retrieveUsersByInterest(interests);
		return convertToUserJSON(users);
	}

	/**
	 * This method is to search by username and return the json response
	 * 
	 * @param username
	 * @param messageUID
	 * @return json response string
	 */
	public String searchUsingUsername(String username, String messageUID) {
		List<User> users = databaseInteractionLayer
				.retrieveUsersByUsername(username);
		List<String> potentielServers = databaseInteractionLayer
				.retrievePotentielServers();
		List<UserJSON> userJSONs = convertToUserJSON(users);
		return Helper
				.buildJSONResponse(userJSONs, potentielServers, messageUID);
	}

	/**
	 * This method is to search by username and return list of users only
	 * 
	 * @param username
	 * @return list of users
	 */
	public List<UserJSON> searchUsingUsername(String username) {
		List<User> users = databaseInteractionLayer
				.retrieveUsersByUsername(username);
		return convertToUserJSON(users);
	}

	/**
	 * This method is to build the userIds depending on SONIC or regular URI
	 * then build UserJSON objects
	 * 
	 * @param users
	 * @return
	 */
	private List<UserJSON> convertToUserJSON(List<User> users) {
		List<String> userIds = null;
		if (PropertiesLookup.isServerFollowSonic()) {
			userIds = sonicUserIdentifier.buildUserIdentifiers(users);
		} else {
			userIds = uriUserIdentifier.buildUserIdentifiers(users);
		}
		List<UserJSON> userJSONs = new ArrayList<UserJSON>();
		for (int i = 0; i < users.size(); i++) {
			userJSONs.add(new UserJSON(userIds.get(i), users.get(i)
					.getUserName()));
		}
		return userJSONs;
	}
}
