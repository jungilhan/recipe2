package com.bulgogi.recipe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.actionbarsherlock.app.SherlockActivity;
import com.bulgogi.recipe.R;

public class SplashActivity extends SherlockActivity {
	private final int DELAY = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_splash);
		
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				finish();
			}
		};
		
		handler.sendEmptyMessageDelayed(0, DELAY);
	}
}
