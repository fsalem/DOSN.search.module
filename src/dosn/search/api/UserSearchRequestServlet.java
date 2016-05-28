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
 * the search requests with username to search locally
 */

@Controller
public class UserSearchRequestServlet {

	@Inject
	LocalSearch localSearch;

	/**
	 * This service receives requests over /users/search in the search
	 * module and returns users with similar username locally.
	 * 
	 * @param response
	 * @param json
	 */
	@RequestMapping(value = { "/users/search" }, method = RequestMethod.POST)
	public void doPost(HttpServletResponse response, @RequestBody String json) {
		System.out.println("new /users/search Handler: ");
		SRRequestJSON requestJSON = Helper.getJSONRequest(json);
		String jsonResponse = localSearch.searchUsingUsername(requestJSON
				.getUsername(), requestJSON.getMessageUID().toString());

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
