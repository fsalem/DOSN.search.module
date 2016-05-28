package dosn.search.api;

import java.util.List;
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
 * the search requests with list of interests to search locally and propagate
 * the request to other servers
 */

@Controller
public class InterestSearchAndPropagateRequestServlet {

	@Inject
	SearchPropagate searchAndPropagate;

	/**
	 * This service receives requests over /interests/searchAndPropagate in the
	 * search module. This service searches locally and propagate the request to
	 * the local potential servers. Then, it collects the users and send it
	 * back.
	 * 
	 * @param request
	 * @param responseURI
	 * @param interests
	 * @param msgID
	 */
	@RequestMapping(value = { "/interests/searchAndPropagate" }, method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void doPost(HttpServletRequest request,
			@RequestParam(value = "responsURI") String responseURI,
			@RequestParam(value = "interests") List<String> interests,
			@RequestParam(value = "msgID") UUID msgID) {

		System.out
				.println("InterestSearchAndPropagateRequestServlet: begin  interests:"
						+ interests
						+ "  msgID:"
						+ msgID
						+ " resultURI: "
						+ responseURI);
		searchAndPropagate.searchAndPropagateByInterests(interests,
				responseURI, msgID);
		System.out.println("InterestSearchAndPropagateRequestServlet: finish");
		// }

	}

}
