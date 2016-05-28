package dosn.search.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import dosn.search.logic.LocalSearch;
import dosn.utility.general.Helper;
import dosn.utility.json.SRRequestJSON;

/**
 * This class is managed by spring framework. It is responsible for receiving
 * the search requests with list of interests to search locally
 */

@Controller
public class InterestSearchRequestServlet {

	@Inject
	LocalSearch localSearch;

	/**
	 * This service receives requests over /interests/search in the search
	 * module and returns users with similar list of interests locally.
	 * 
	 * @param response
	 * @param json
	 */
	@RequestMapping(value = { "/interests/search" }, method = RequestMethod.POST)
	public void doPost(HttpServletResponse response, @RequestBody String json) {
		System.out.println("new /interests/search Handler: ");
		SRRequestJSON requestJSON = Helper.getJSONRequest(json);
		String jsonResponse = localSearch.searchUsingInterest(Helper
				.convertInterestJSONListToString(requestJSON.getInterests()),
				requestJSON.getMessageUID().toString());

		response.setContentType("application/json");

		try {
			PrintWriter out;
			out = response.getWriter();
			out.print(jsonResponse);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
