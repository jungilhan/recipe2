package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ScrollViewDelegate;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.ThumbnailAdapter;
import com.bulgogi.recipe.application.RecipeApplication;
import com.bulgogi.recipe.auth.FacebookHelper;
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.http.NodeRestApi;
import com.bulgogi.recipe.http.WPRestApi;
import com.bulgogi.recipe.http.model.Count;
import com.bulgogi.recipe.http.model.Post;
import com.bulgogi.recipe.http.model.Posts;
import com.bulgogi.recipe.model.Thumbnail;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.ProfilePictureView;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localytics.android.LocalyticsSession;

public class HomeActivity extends SherlockActivity implements Session.StatusCallback, ActionBar.OnNavigationListener {
	private static final String TAG = HomeActivity.class.getSimpleName();

	private LocalyticsSession localyticsSession;

	private ArrayList<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
	private HashMap<Integer, Count> countMap = new HashMap<Integer, Count>();
	private GridView gvThumbnail;
	private ThumbnailAdapter adapter;
	private PullToRefreshAttacher pullToRefreshAttacher;
	private FacebookHelper facebookHelper;
	private MenuItem actionbarLogin;
	private boolean isLoading = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_home);

		facebookHelper = new FacebookHelper(this, savedInstanceState, this);

		setupViews();
		requestCountInfo();
		requestRecipe(Constants.QUERY_COUNT, true);

		localyticsSession = new LocalyticsSession(this);
		localyticsSession.open();
		localyticsSession.upload();
	}

	private void setupViews() {
		getSupportActionBar().setIcon(R.drawable.abs_icon);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.ar_sort, R.layout.tv_sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        
		adapter = new ThumbnailAdapter(this, thumbnails);
		gvThumbnail = (GridView) findViewById(R.id.sgv_thumbnail);

		int columns = RecipeApplication.isTablet() == true ? Constants.GRIDVIEW_TABLET_COLUMNS : Constants.GRIDVIEW_DEFAULT_COLUMNS;
		gvThumbnail.setNumColumns(columns);
		gvThumbnail.setAdapter(adapter);

		pullToRefreshAttacher = new PullToRefreshAttacher(this);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			pullToRefreshAttacher.setRefreshableView(gvThumbnail, (PullToRefreshAttacher.ViewDelegate) new ScrollViewDelegate(),
					new PullToRefreshAttacher.OnRefreshListener() {
				@Override
				public void onRefreshStarted(View view) {
					requestCountInfo();
					requestRecipe(Constants.QUERY_COUNT, false);
				}
			});
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		facebookHelper.addSessionStatusCallback(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		facebookHelper.removeSessionStatusCallback(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebookHelper.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		facebookHelper.saveSession(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		localyticsSession.open();
	}

	@Override
	protected void onPause() {
		localyticsSession.close();
		localyticsSession.upload();
		super.onPause();

		if (android.os.Build.VERSION.SDK_INT >= 14) {
			pullToRefreshAttacher.setRefreshComplete();
		}
	}

	private void requestRecipe(int count, boolean showProgressBar) {
		if (isLoading) {
			return;
		} else {
			isLoading = true;
		}

		findViewById(R.id.pb_main_loading).setVisibility(showProgressBar == true ? View.VISIBLE : View.GONE);
		new RecipeLoader().execute(WPRestApi.getPostsUrl(count, false), showProgressBar);
	}

	private void requestCountInfo() {
		new CountInfoLoader().execute(NodeRestApi.getCountInfoUrl());
	}

	private class RecipeLoader extends AsyncTask {
		private LinearLayout llError = (LinearLayout) findViewById(R.id.ll_error);
		private ProgressBar pbLoading = (ProgressBar) findViewById(R.id.pb_main_loading);

		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			Posts posts = null;

			try {
				posts = mapper.readValue(new URL((String) params[0]), Posts.class);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return posts;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			llError.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(Object result) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				pullToRefreshAttacher.setRefreshComplete();
			}

			isLoading = false;

			if (pbLoading.getVisibility() == View.VISIBLE) {
				pbLoading.setVisibility(View.GONE);
			}

			if (result == null) {
				llError.setVisibility(View.VISIBLE);
				return;
			} else {
				llError.setVisibility(View.GONE);
				gvThumbnail.setVisibility(View.VISIBLE);
			}

			Posts posts = (Posts) result;
			// Log.d(TAG, posts.toString());

			for (int i = 0; i < posts.posts.size(); i++) {
				Post post = posts.posts.get(i);
				Thumbnail thumbnail = new Thumbnail(post);
				if (contains(post)) {
					remove(post);
				}

				thumbnails.add(thumbnail);
			}

			Collections.sort(thumbnails, new Comparator<Thumbnail>() {
				public int compare(Thumbnail s1, Thumbnail s2) {
					if (s1.getId() > s2.getId())
						return -1;
					else if (s1.getId() < s2.getId())
						return 1;
					else
						return 0;
				}
			});

			updateCountInfo();

			adapter.notifyDataSetChanged();
		}

		private void remove(Post post) {
			Iterator<Thumbnail> iter = thumbnails.iterator();
			while (iter.hasNext()) {
				Thumbnail thumbnail = (Thumbnail) iter.next();
				if (thumbnail.getId() == post.id) {
					thumbnails.remove(thumbnail);
					return;
				}
			}
		}

		private boolean contains(Post post) {
			Iterator<Thumbnail> iter = thumbnails.iterator();
			while (iter.hasNext()) {
				if (((Thumbnail) iter.next()).getId() == post.id) {
					return true;
				}
			}
			return false;
		}
	}

	private class CountInfoLoader extends AsyncTask {
		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Count> counts = null;

			try {
				counts = mapper.readValue(new URL((String) params[0]), new TypeReference<List<Count>>() {
				});
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return counts;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result == null) {
				return;
			}

			ArrayList<Count> counts = (ArrayList<Count>) result;
			Log.d(TAG, counts.toString());
			for (Count count : counts) { 
				countMap.put(count.postId, count);	
			}

			updateCountInfo();

			//if (thumbnails.size() > 0) {
				//adapter.notifyDataSetChanged();
			//}
		}
	}

	private void updateCountInfo() {
		for (Thumbnail thumbnail : thumbnails) {
			int postId = thumbnail.getId();
			Count count = countMap.get(postId);
			if (count != null) {
				thumbnail.setLikeCount(count.likes);
				thumbnail.setCommentCount(count.comments);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		actionbarLogin = menu.findItem(R.id.action_login);

		if (android.os.Build.VERSION.SDK_INT >= 14) {
			MenuItem refresh = menu.findItem(R.id.action_refresh);
			refresh.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			if (facebookHelper.isLogin()) {
				actionbarLogin.setTitle(R.string.action_logout);
			} else {
				actionbarLogin.setTitle(R.string.action_login);
			}
        	break;
        case R.id.action_login:
        	if (!facebookHelper.isLogin()) {
        		facebookHelper.login();
        	} else {
        		showLogoutDialog();
        	}
        	break;
        case R.id.action_refresh:
        	requestCountInfo();
        	requestRecipe(Constants.QUERY_COUNT, true);
        	break;
        }
        return super.onOptionsItemSelected(item);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showLogoutDialog() {
		View innerView = getLayoutInflater().inflate(R.layout.rl_logout, null);

		if (facebookHelper.getId() != null) {
			ProfilePictureView ppvProfile = (ProfilePictureView) innerView.findViewById(R.id.ppv_profile);
			ppvProfile.setProfileId(facebookHelper.getId());
		}

		if (facebookHelper.getName() != null) {
			TextView tvName = (TextView) innerView.findViewById(R.id.tv_name);
			tvName.setText(facebookHelper.getName());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(innerView);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				facebookHelper.logout();
			}
		});

		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});

		builder.show();
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			facebookHelper.makeMeRequest(session);
		}
	}

	public void onRefreshClicked(View v) {
		requestCountInfo();
		requestRecipe(Constants.QUERY_COUNT, true);
	}
	
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }
}
