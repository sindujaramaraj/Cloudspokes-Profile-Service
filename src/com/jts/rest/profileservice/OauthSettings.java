package com.jts.rest.profileservice;

/**
 * Class containing static members/settings used by the application
 * 
 * @author Jeff Douglas
 */

public class OauthSettings {
  
  // Consumer settings copied from Salesforce.com Remote Access application
  public static String CONSUMER_KEY = "3MVG9QDx8IX8nP5TK_9n6SRw_NE9gc0pyTDyXiYreviL_Aimlq400bdQ1qUTVaXGGJMsbB2zMeBp0HhvLInwn";
  public static String CONSUMER_SECRET = "5722104747573906655";
  
  // URLs used by the application - change to test.salesforce.com for sandboxes
  public static String HOST = "https://login.salesforce.com";
  
  // URLs used by the application
  public static String URL_CALLBACK = "https://cloudspokes-profile-service.appspot.com/callback";
  public static String URL_API_LOGIN = HOST+"/services/OAuth/c/17.0";
  public static String URL_AUTHORIZATION = HOST+"/setup/secur/RemoteAccessAuthorizationPage.apexp";  
  public static String URL_AUTH_ENDPOINT = HOST+"/services/Soap/u/17.0";
  public static String URL_REQUEST_TOKEN = HOST+"/_nc_external/system/security/oauth/RequestTokenHandler";
  public static String URL_ACCESS_TOKEN = HOST+"/_nc_external/system/security/oauth/AccessTokenHandler";

}
