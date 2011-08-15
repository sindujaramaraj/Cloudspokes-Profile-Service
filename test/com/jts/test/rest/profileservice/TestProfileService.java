package com.jts.test.rest.profileservice;

import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Assert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

import com.jts.rest.profileservice.utils.OauthHelperUtils;

public class TestProfileService {

	// hard code data here
	private static final String username = "test1";

	private static final String sessionid = "00DU0000000HXKn!ARkAQNkniD_Lrgp4yokn9M1k72Ck2VWlQsKG5npXtY2luVddZXc1poLlQDhA1tW1t01y.cy6J5MSf_IRLpvTXNNybX17mBi5";

	private static final String url = "https://cloudspokes-profile-service.appspot.com/profile";

	@Test
	public void NoUserNameTest() throws Exception {
		JSONObject response = makeRequest("", sessionid);
		Assert.assertEquals(true, response.get("status").equals("failure"));
	}
	
	@Test
	public void NoSessionIdTest() throws Exception {
		JSONObject response = makeRequest(username, "");
		Assert.assertEquals(true, response.get("status").equals("failure"));
	}
	
	@Test
	public void ResponseTest() throws Exception {
		JSONObject response = makeRequest(username, sessionid);
		Assert.assertEquals("success", response.get("status"));
		JSONArray memberArr = (JSONArray) response.get("member");
		JSONObject member = (JSONObject) memberArr.get(0);
		Assert.assertEquals(true, member.get("Username__c").equals(username));
		Assert.assertNotNull(response.get("recommendations"));
		JSONObject challenge = (JSONObject) response.get("challenges");
		Assert.assertNotNull(challenge.get("activeChallenges"));
		Assert.assertNotNull(challenge.get("pastChallenges"));
		Assert.assertNotNull(challenge.get("totalChallenges"));
	}

	private JSONObject makeRequest(String username, String sessionId) throws Exception {
		URL endpoint = new URL(url + "?username=" + username);
		HttpURLConnection urlc = (HttpURLConnection) endpoint.openConnection();
		urlc.setRequestProperty("session-id", sessionId);
		urlc.setRequestMethod("GET");
		urlc.setDoOutput(true);
		String output = OauthHelperUtils.readInputStream(urlc.getInputStream());
		urlc.disconnect();
		Object obj = JSONValue.parse(output);
		JSONObject jsonObj = (JSONObject) obj;
		return jsonObj;
	}
}
