package com.bulgogi.recipe.http;

public class NodeRestApi {
	private static final String BASE_URL = "http://14.63.219.181:3000/";
	private static final String POST_COMMENT = "comment/write";
	private static final String REQUEST_COMMENTS = "comment/load/";
	private static final String REQUEST_LIKE_USER = "like/";
	private static final String POST_PAGEVIEW = "count/";
	private static final String POST_LIKE = "like";
	private static final String POST_UNLIKE = "unlike";
	private static final String REQUEST_COUNT_INFO = "count_info";

	public static String getPostCommentUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(POST_COMMENT);
		return url.toString();
	}
	
	public static String getCommentsUrl(int postId) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(REQUEST_COMMENTS + postId);
		return url.toString();
	}
	
	public static String getLikeUsersUrl(int postId) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(REQUEST_LIKE_USER + postId);
		return url.toString();
	}
	
	public static String getPostPageViewUrl(int postId) {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(POST_PAGEVIEW + postId);
		return url.toString();
	}
	
	public static String getPostLikeUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(POST_LIKE);
		return url.toString();
	}
	
	public static String getPostUnLikeUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(POST_UNLIKE);
		return url.toString();
	}
	
	public static String getCountInfoUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append(REQUEST_COUNT_INFO);
		return url.toString();
	}	
}
