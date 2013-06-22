package com.bulgogi.recipe.application;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class RecipeApplication extends Application {
	@Override
    public void onCreate() {
		super.onCreate();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		.memoryCacheSize((int)(Runtime.getRuntime().maxMemory() / 8))
		.build();
		
		ImageLoader.getInstance().init(config);
	}
}
