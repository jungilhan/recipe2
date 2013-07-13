package com.bulgogi.recipe.application;

import android.app.Application;
import android.preference.PreferenceManager;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.utils.PreferenceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class RecipeApplication extends Application {
	private static boolean isTablet;

	@Override
	public void onCreate() {
		super.onCreate();

		isTablet = getResources().getBoolean(R.bool.is_tablet);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.memoryCacheSize((int) (Runtime.getRuntime().maxMemory() / 8)).build();
		ImageLoader.getInstance().init(config);

		PreferenceHelper.getInstance().init(PreferenceManager.getDefaultSharedPreferences(this));
	}

	public static boolean isTablet() {
		return isTablet;
	}
}
