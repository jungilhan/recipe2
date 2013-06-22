package com.bulgogi.recipe.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.actionbarsherlock.app.SherlockActivity;
import com.bulgogi.recipe.R;

public class SplashActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_splash);
		
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				finish();
			}
		};
		
		handler.sendEmptyMessageDelayed(0, 2000);
	}
}
