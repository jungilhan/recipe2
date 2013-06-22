package com.bulgogi.recipe.http;

public class WPRestApi {
	private static final String WP_BASE_URL = "http://14.63.219.181/";
	private static final String WP_POSTS_URI = "?json=1";
	private static final String WP_POSTS_PARAM_COUNT = "&count=";
	private static final String WP_POSTS_PARAM_DESC = "&order=DESC";
	private static final String WP_POSTS_PARAM_ASC = "&order=ASC";
	
	public static String getPostsUrl(int count, boolean desc) {
		StringBuilder url = new StringBuilder(WP_BASE_URL);
		url.append(WP_POSTS_URI);
		url.append(WP_POSTS_PARAM_COUNT + count);
		if (desc) {
			url.append(WP_POSTS_PARAM_DESC);
		} else {
			url.append(WP_POSTS_PARAM_ASC);
		}
		
		return url.toString();
	}
}
