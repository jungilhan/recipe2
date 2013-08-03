package com.bulgogi.recipe.http;

public class NaverRestApi {
	private static final String BASE_URL = "http://openapi.naver.com/search?key=0412fca944c89cbaea5e638184fa3e83";
	private static final String TARGET = "&target=blog";
	private static final String SORT = "&sort=sim";
	private static final String QUERY = "&query=";
	private static final String DISPLAY = "&display=";
	private static final String START = "&start=";
	

	public static String getBlogSearchUrl(String query, int display, int start) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(QUERY + query);
		url.append(DISPLAY + display);
		url.append(START + start);
		url.append(TARGET);
		url.append(SORT);
		return url.toString();
	}
}
