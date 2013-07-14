package com.bulgogi.recipe.http;

public class FBRestApi {
	private static final String BASE_URL = "https://graph.facebook.com/";
	private static final String REQUEST_NAME = "?fields=name";

	public static String getPostCommentUrl(long facebookId) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(facebookId);
		url.append(REQUEST_NAME);
		return url.toString();
	}
}
