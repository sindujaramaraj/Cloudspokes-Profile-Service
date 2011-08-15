package com.jts.rest.profileservice.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.jts.rest.profileservice.PSContants;

/**
 * Helper class for accessing sales force rest apis 
 * @author sinduja
 */

public class RestServiceHelper {
	
	private static final Logger logger = Logger.getLogger(RestServiceHelper.class.getName());

	/**
	 * Accesses Member service
	 * Returns the the member object with given username
	 * @param sessionId
	 * @param memberId
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray getMember(String sessionId, String memberId) throws MalformedURLException, IOException {
		String fields = "Total_1st_Place__c,Total_2nd_Place__c,Total_3st_Place__c,Challenges_Entered__c,"
			+ "Challenges_Lost__c,Challenges_Submitted__c,Challenges_Won__c,Company__c,"
			+ "Country__c,First_Name__c,Gender__c,Last_Name__c,Quote__c,Summary_Bio__c,Total_Badges__c,"
			+ "Total_Money__c,Total_Points__c,Username__c";
			 
		return makeGetRequest(sessionId, PSContants.MEMBERS_URL + "/" + memberId + "?fields=" + fields);
	}
	
	/**
	 * Accesses MemberChallenge service
	 * Returns all challenges for a given user
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray getChallenges(String sessionId, String username) throws MalformedURLException, IOException {
		return makeGetRequest(sessionId, PSContants.MEMBERS_URL + "/" + username + "/challenges");
	}
	
	/**
	 * Accesses Participant service
	 * Returns recent 10 challenges for a user based on end date
	 * Assumption is that the recent 10 challenges will contain all active challenges and atleast 3 past challenges 
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray getRecentParticipantChallengesByUsername(String sessionId, String username) throws MalformedURLException, IOException {
		String challengeFields = "challenge__r.id,challenge__r.name,challenge__r.description__c,challenge__r.status__c,"
			+ "challenge__r.days_till_close__c,challenge__r.license__c,challenge__r.top_prize__c,";
		String challengeParticipantFields = "name,has_entry__c,place__c,money_awarded__c,status__c";
		
		return makeGetRequest(sessionId, PSContants.CHALLENGE_PARTICIPANT_URL + "?membername=" + username
				+"&fields=" + challengeFields + challengeParticipantFields
				+"&limit=10"
				+"&orderby=challenge__r.end_date__c+desc");
	}
	
	/**
	 * Accesses Recommendation service
	 * Gets all recommendations for a user
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray getRecommendationsByUser(String sessionId, String username) throws MalformedURLException, IOException {
		String fields = "Recommendation__c,Recommendation_From__c";
		return makeGetRequest(sessionId, PSContants.RECOMMENDATIONS_URL + "?search=" + username + "&fields=" + fields);
	}
	
	/**
	 * Accesses ChallengeCategory service
	 * Returns the categories associated to a particular challenge
	 * This service is not working currently
	 * @param sessionId
	 * @param challengeId
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray getCategoriesByChallenge(String sessionId, String challengeId) throws MalformedURLException, IOException {
		String fields = "category__r.name,category__r.color__c,category__r.color_hex__c";
		return makeGetRequest(sessionId, PSContants.CHALLENGES_URL + "/" + challengeId + "/categories"
				+ "?fields="+ fields);
	}
	
	/**
	 * Gets the recent challenges and splits them to active and past challenges
	 * Maximum 3 past challenges and all active challenges will be returned
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getProcessedChallengesM2(String sessionId, String username) throws MalformedURLException, IOException {
		JSONArray recentChallenges = getRecentParticipantChallengesByUsername(sessionId, username);
		JSONArray activeChallenges = new JSONArray();
		JSONArray pastChallenges = new JSONArray();
		
		Iterator<JSONObject> iterator = recentChallenges.iterator();
		while (iterator.hasNext()) {
			JSONObject participantChallenge = iterator.next();
			JSONObject challenge = (JSONObject)participantChallenge.get("Challenge__r");
			if (challenge.get("Status__c").equals(PSContants.STATUS_CREATED)) { //active challenge
				activeChallenges.add(participantChallenge);
				JSONArray categories = getCategoriesByChallenge(sessionId, (String)participantChallenge.get("Challenge__c"));
				participantChallenge.put("categories", categories);
			} else { //past challenges
				if (pastChallenges.size() < 3) {
					pastChallenges.add(participantChallenge);
					JSONArray categories = getCategoriesByChallenge(sessionId, (String)participantChallenge.get("Challenge__c"));
					participantChallenge.put("categories", categories);
				}
			}
		}
		JSONObject challenges = new JSONObject();
		challenges.put("activeChallenges", activeChallenges);
		challenges.put("pastChallenges", pastChallenges);
		challenges.put("totalChallenges", getChallengeCountByUser(sessionId, username));
		return challenges;
	}
	
	/**
	 * Returns the count of challenges for a given user
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static int getChallengeCountByUser(String sessionId, String username) throws MalformedURLException, IOException {
		JSONArray challenges = getChallenges(sessionId, username);
		return challenges.size();
	}
	
	/**
	 * Uses MemberChallenge service to get all the associated challenge information for a user
	 * Sorts the challenge information with end date
	 * Returns all the active challenges and 3 most recent past challenge 
	 * @param sessionId
	 * @param username
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getProcessedChallengesM1(String sessionId, String username) throws MalformedURLException, IOException {
		//get all challenges
		JSONArray challenges = getChallenges(sessionId, username);
		
		JSONArray activeChallenges = new JSONArray();
		JSONArray pastChallenges = new JSONArray();
		SortedMap<Long, JSONObject> pastChallengesByDate = new TreeMap<Long, JSONObject>(); 
		Iterator<JSONObject> iterator = challenges.iterator();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-ddhh:mm:ss");
		
		while (iterator.hasNext()) {
			JSONObject challenge = iterator.next();
			//identify active challenge with status
			if (challenge.get("Status__c").equals(PSContants.STATUS_CREATED)) {
				activeChallenges.add(challenge);
			} else {
				String endDateStr = ((String)challenge.get("End_Date__c")).replace("T", "");
				Date date = new Date();
				try {
					date = dateFormat.parse(endDateStr);
				} catch(ParseException pe) {
					logger.log(Level.SEVERE, "Error occurent while parsing date " + endDateStr);
				}
				pastChallengesByDate.put(date.getTime(), challenge);
			}
		}
		
		//from the sorted map extract the recent challenge
		int pastChallengeSize = pastChallengesByDate.size();
		if (pastChallengeSize > 0) {
			Object[] challengeArr = (Object[]) pastChallengesByDate.values().toArray();
			int startIndex = pastChallengeSize > 3 ? pastChallengeSize - 3 : 0;
			for (int i = startIndex; i < pastChallengeSize; i++) {
				pastChallenges.add(challengeArr[i]);
			}
		}
		JSONObject resultChallenges = new JSONObject();
		resultChallenges.put("activeChallenges", activeChallenges);
		resultChallenges.put("pastChallenges", pastChallenges);
		resultChallenges.put("totalChallenges", challenges.size());
		return resultChallenges;
	}

	/**
	 * Makes a get request to the given url and returns the json object
	 * @param sessionId
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONArray makeGetRequest(String sessionId, String url) throws MalformedURLException, IOException {
		URL endpoint = new URL(url);
		HttpURLConnection urlc = (HttpURLConnection) endpoint.openConnection();
		urlc.setRequestProperty("Authorization", "OAuth " + sessionId);
		urlc.setRequestMethod("GET");
		urlc.setDoOutput(true);
		String output = OauthHelperUtils.readInputStream(urlc.getInputStream());
		urlc.disconnect();
		Object json = JSONValue.parse(output);
		JSONArray jsonArr = (JSONArray)json;
		return jsonArr;
	}

}
