package com.bulgogi.recipe.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.config.Constants.Extra;
import com.nbpcorp.mobilead.sdk.MobileAdListener;
import com.nbpcorp.mobilead.sdk.MobileAdView;

public class WebViewActivity extends SherlockActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ac_webview);

		setupViews();
	}
	
	private void setupViews() {
		Intent intent = getIntent();
		String url = (String) intent.getStringExtra(Extra.WEBVIEW_URL);
		String title = (String) intent.getStringExtra(Extra.WEBVIEW_TITLE);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(title);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		final ProgressBar pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
		
		WebView webView = (WebView) findViewById(R.id.wv_blog);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				pbLoading.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				pbLoading.setVisibility(View.GONE);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(WebViewActivity.this, "에러가 발생했습니다. " + description, Toast.LENGTH_SHORT).show();
			}
		});
		
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				pbLoading.setProgress(progress);
			}
		});
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
		
		final MobileAdView adView = (MobileAdView) findViewById(R.id.adview);
		adView.setListener(new MobileAdListener() {
			@Override
			public void onReceive(int error) {
				if (error == -1 || error == 3 || error == 4 || error == 5 || error == 101
						|| error == 102 || error == 103 || error == 105 || error == 106) {
					adView.setVisibility(View.GONE);
				} else {
					adView.setVisibility(View.VISIBLE);
				}
			}
		});
		adView.start();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
