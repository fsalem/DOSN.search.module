package dosn.search.api;

import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import dosn.search.logic.SearchPropagate;

/**
 * This class is managed by spring framework. It is responsible for receiving
 * the search requests with username to search locally and propagate the request
 * to other servers
 */

@Controller
public class UserSearchAndPropagateRequestServlet {

	@Inject
	SearchPropagate searchAndPropagate;

	/**
	 * This service receives requests over /users/searchAndPropagate in the
	 * search module. This service searches locally according to a username and
	 * propagate the request to the local potential servers. Then, it collects
	 * the users and send it back.
	 * 
	 * @param request
	 * @param responseURI
	 * @param username
	 * @param msgID
	 */
	@RequestMapping(value = { "/users/searchAndPropagate" }, method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void doPost(HttpServletRequest request,
			@RequestParam(value = "responsURI") String responseURI,
			@RequestParam(value = "username") String username,
			@RequestParam(value = "msgID") UUID msgID) {
		System.out
				.println("UserSearchAndPropagateRequestServlet: begin  username:"
						+ username
						+ "  msgID:"
						+ msgID
						+ " resultURI: "
						+ responseURI);
		searchAndPropagate.searchAndPropagateByUsername(username, responseURI,
				msgID);
		System.out.println("UserSearchAndPropagateRequestServlet: finish");

		// }

	}

}
