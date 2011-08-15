package com.jts.rest.profileservice;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

import com.jts.rest.profileservice.utils.OauthHelperUtils;

/**
 * Callback servlet that will be called after login from Salesforce
 * Using oauth token fetches the session-id and stores in http session
 * It the redirects to profile url
 * 
 * @author Sinduja
 */

@SuppressWarnings("serial")
public class CallbackServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(CallbackServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	
	  String username = req.getParameter("username"); 
	
	  try {
	
	      String oauthToken = (String) req.getParameter(OAuth.OAUTH_TOKEN);
	      String oauthConsumerKey = (String) req.getParameter(OAuth.OAUTH_CONSUMER_KEY);
	      String oauthVerifier = (String) req.getParameter("oauth_verifier");
	
	      log.info("oauthToken=" + oauthToken);
	      log.info("oauthConsumerKey=" + oauthConsumerKey);
	      log.info("oauthVerifier=" + oauthVerifier);
	
	      OAuthAccessor accessor = new OAuthAccessor(new OAuthConsumer(
	          OauthSettings.URL_CALLBACK, OauthSettings.CONSUMER_KEY,
	          OauthSettings.CONSUMER_SECRET, null));
	      accessor.requestToken = oauthToken;
	      accessor.tokenSecret = OauthHelperUtils.REQUEST_TOKENS.get(oauthToken);
	      log.info("requestToken=" + accessor.requestToken);
	
	      String response = OauthHelperUtils.getAccessToken(accessor,
	          oauthVerifier, OauthSettings.URL_ACCESS_TOKEN);
	
	      log.info("access token=" + accessor.accessToken);
	      log.info("access token secret=" + accessor.tokenSecret);
	      log.info("response=" + response);
	      log.info("Logging into Salesforce.com since now have the access tokens");
	      
	      String loginResponse = OauthHelperUtils.getNewSfdcSession(accessor,
	              OauthSettings.URL_API_LOGIN);
	      //get the session id from response
	      if (loginResponse.startsWith("<")) {
	    	  OauthHelperUtils.XmlResponseHandler xmlHandler = OauthHelperUtils
	              .parseResponse(loginResponse);
	          String sessionId = xmlHandler.getSessionId();
	          log.info("sessionId=" + sessionId);
	          //store the session id in session
	          req.getSession().setAttribute("session-id", sessionId);
	      }
	      
	      resp.sendRedirect(PSContants.PROFILE_URL + "?username=" + username);
	  
	  } catch (Exception e) {
	      log.info("Callback servlet exception=" + e.toString());
	  }
  }

}
