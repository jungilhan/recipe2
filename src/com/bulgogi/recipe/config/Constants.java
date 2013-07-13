package com.bulgogi.recipe.config;

public class Constants {
	private Constants() {}
	
	public static final String PREF_NAME = "name";
	public static final String PREF_FACEBOOK_ID = "facebook_id";
	public static final int QUERY_COUNT = 100;
	public static final int GRIDVIEW_DEFAULT_COLUMNS = 2;
	public static final int GRIDVIEW_TABLET_COLUMNS = 3;
	
	public static class Config {
		public static final boolean DEBUG = false;
	}
	
	public static class Extra {
		public static final String POST = "com.bulgogi.recipe.extra.POST";
	}
}
