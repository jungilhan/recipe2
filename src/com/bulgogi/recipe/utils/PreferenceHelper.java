package com.bulgogi.recipe.utils;

import android.content.SharedPreferences;

public class PreferenceHelper {
	private static final String TAG = PreferenceHelper.class.getSimpleName();

	SharedPreferences settings;
	private volatile static PreferenceHelper instance;

	public static PreferenceHelper getInstance() {
		if (instance == null) {
			synchronized (PreferenceHelper.class) {
				if (instance == null) {
					instance = new PreferenceHelper();
				}
			}
		}
		return instance;
	}

	private PreferenceHelper() {};

	public void init(SharedPreferences settings) {		
		this.settings = settings;
	}

	public boolean contains(String key) {
		return settings.contains(key); 
	}
	
	public String getString(String key, String defValue) {
		return settings.getString(key, defValue);
	}

	public Integer getInt(String key, Integer defValue) {
		return settings.getInt(key, defValue);
	}

	public Long getLong(String key, Long defValue) {
		return settings.getLong(key, defValue);
	}

	public Boolean getBoolean(String key, Boolean defValue) {
		return settings.getBoolean(key, defValue);
	}

	public Float getFloat(String key, Float defValue) {
		return settings.getFloat(key, defValue);
	}

	public void putString(String key, String value) {
		SharedPreferences.Editor editor = settings.edit();
		if (value != null) {
			editor.putString(key, value);
		} else {
			editor.remove(key);
		}
		editor.commit();
	}

	public void putLong(String key, Long value) {
		SharedPreferences.Editor editor = settings.edit();
		if (value != null) {
			editor.putLong(key, value);
		} else {
			editor.remove(key);
		}
		editor.commit();
	}

	public void putBoolean(String key, Boolean value) {
		SharedPreferences.Editor editor = settings.edit();
		if (value != null) {
			editor.putBoolean(key, value);
		} else {
			editor.remove(key);
		}
		editor.commit();
	}

	public void putInt(String key, Integer value) {
		SharedPreferences.Editor editor = settings.edit();
		if (value != null) {
			editor.putInt(key, value);
		} else {
			editor.remove(key);
		}
		editor.commit();
	}

	public void putFloat(String key, Float value) {
		SharedPreferences.Editor editor = settings.edit();
		if (value != null) {
			editor.putFloat(key, value);
		} else {
			editor.remove(key);
		}
		editor.commit();
	}
}
