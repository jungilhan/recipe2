package com.bulgogi.recipe.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.LikeUserAdapter;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.FBRestApi;
import com.bulgogi.recipe.http.model.Like;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class LikeUserActivity extends SherlockListActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_like_user);

		Intent intent = getIntent();
		ArrayList<Like> likeUsers = (ArrayList<Like>) intent.getSerializableExtra(Extra.LIKE_USERS);

		setupViews(likeUsers);
	}
	
	private void setupViews(ArrayList<Like> likeUsers) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(R.string.like_user_title);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		getListView().setVisibility(View.INVISIBLE);
		
		final LikeUserAdapter adapter = new LikeUserAdapter(this, likeUsers);
		setListAdapter(adapter);
		
		final ProgressBar pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

		final Like firstItem = likeUsers.get(0);
		for (int i = 0; i < likeUsers.size(); i++) {
			final Like user = likeUsers.get(i);	
			final int index = i; 
		
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(FBRestApi.getNameUrl(user.facebookId), new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
					try {
						JSONObject json = new JSONObject(response);
						if (json != null && json.has("name")) {
							user.name = json.getString("name");
						} else {
							user.name = "정보를 불러오지 못했습니다.";
						}

						updateItemAtPosition(index);
						if (user.equals(firstItem)) {
							getListView().setVisibility(View.VISIBLE);
							pbLoading.setVisibility(View.GONE);	
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
			    }
			});
		}
	}
	
	private void updateItemAtPosition(int position) {
	    int visiblePosition = getListView().getFirstVisiblePosition();
	    View view = getListView().getChildAt(position - visiblePosition);
	    getListView().getAdapter().getView(position, view, getListView());
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
