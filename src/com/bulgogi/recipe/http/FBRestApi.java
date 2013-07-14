package com.bulgogi.recipe.http;

public class FBRestApi {
	private static final String BASE_URL = "https://graph.facebook.com/";
	private static final String REQUEST_NAME = "?fields=name";
	private static final String REQUEST_PROFILE = "/picture?type=normal";

	public static String getNameUrl(long facebookId) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(facebookId);
		url.append(REQUEST_NAME);
		return url.toString();
	}
	
	public static String getProfileUrl(long facebookId) {		
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(facebookId);
		url.append(REQUEST_PROFILE);
		return url.toString();
	} 
}
