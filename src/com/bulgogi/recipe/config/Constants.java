package com.bulgogi.recipe.config;

public class Constants {
	private Constants() {
	}

	public static final String PREF_NAME = "name";
	public static final String PREF_FACEBOOK_ID = "facebook_id";
	public static final String PREF_SORT_TYPE = "sort_type";

	public static final int WP_RECIPE_QUERY_COUNT = 100;
	public static final int NAVER_BLOG_QUERY_COUNT = 30;

	public static final int GRIDVIEW_DEFAULT_COLUMNS = 2;
	public static final int GRIDVIEW_TABLET_COLUMNS = 3;

	public static final int SORT_BY_NEWEST = 0;
	public static final int SORT_BY_VIEW_COUNT = 1;
	public static final int SORT_BY_LIKE_COUNT = 2;
	public static final int SORT_BY_COMMENT_COUNT = 3;

	public static class Config {
		public static final boolean DEBUG = false;
	}

	public static class Extra {
		public static final String POST = "com.bulgogi.recipe.extra.POST";
		public static final String LIKE_USERS = "com.bulgogi.recipe.extra.LIKE_USERS";
		public static final String BLOG_SEARCH_RESULT = "com.bulgogi.recipe.extra.BLOG_SEARCH_RESULT";
		public static final String BLOG_SEARCH_RESULT_TITLE = "com.bulgogi.recipe.extra.BLOG_SEARCH_RESULT_TITLE";
		public static final String WEBVIEW_TITLE = "com.bulgogi.recipe.extra.WEBVIEW_TITLE";
		public static final String WEBVIEW_URL = "com.bulgogi.recipe.extra.WEBVIEW_URL";
	}
}
