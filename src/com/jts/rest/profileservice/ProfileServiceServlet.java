package com.jts.rest.profileservice;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.jts.rest.profileservice.utils.RestServiceHelper;
import com.jts.rest.profileservice.utils.Util;

/**
 * This service is used to get the profile information for a user.
 * The profile information contains users personal info, recent challenges info and recommendations
 * Request: https://cloudspokes-profile-service.appspot.com/profile?username=some-username
 * This request can be accessed via URL or get
 * When accessing via url if session-id is not found then it redirects to login and obtains a session id
 * If accessed via get request then send "session-id" in header
 * 
 * 
 * @author sinduja
 *
 */

@SuppressWarnings("serial")
public class ProfileServiceServlet extends HttpServlet {

	protected static final Logger log = Logger.getLogger(ProfileServiceServlet.class
		      .getName());

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		HttpSession session = req.getSession();
		//get username from url
		String username = req.getParameter("username");
		boolean urlaccess = "true".equals(req.getParameter("urlaccess"));
		//result object
		resp.setContentType("text/plain");
		JSONObject result = new JSONObject();
		
		//return error if username is null
		if (Util.isBlank(username)) {
			result.put("status", "failure");
			result.put("message", "Error: Username is required");
			resp.getWriter().write(result.toString());
			return;
		}
		//get session id from url or header
		String sessionId = (String)session.getAttribute("session-id");
		if (Util.isBlank(sessionId)) { //get sessionId from header
			sessionId = req.getHeader("session-id");
		}
		//construct the required json if session id is found
		if (!Util.isBlank(sessionId)) {
			result.put("member", RestServiceHelper.getMember(sessionId, username));
			result.put("challenges", RestServiceHelper.getProcessedChallengesM1(sessionId, username));
			result.put("recommendations", RestServiceHelper.getRecommendationsByUser(sessionId, username));
			result.put("status", "success");
			result.put("session-id", sessionId); //just in case sending the sessionid
			resp.getWriter().write(result.toString());
		} else { //redirect for login or throw error
			if (!urlaccess) {
				result.put("status", "failure");
				result.put("message", "Error: session-id is required in header");
				resp.getWriter().write(result.toString());
			} else {
				resp.sendRedirect(PSContants.LOGIN_URL + "?username=" + username);				
			}
		}		
		
	}
}