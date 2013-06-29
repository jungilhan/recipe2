package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.StaggeredGridView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.ThumbnailAdapter;
import com.bulgogi.recipe.auth.FacebookHelper;
import com.bulgogi.recipe.http.WPRestApi;
import com.bulgogi.recipe.http.model.Post;
import com.bulgogi.recipe.http.model.Posts;
import com.bulgogi.recipe.model.Thumbnail;
import com.facebook.Session;
import com.facebook.SessionState;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HomeActivity extends SherlockActivity {
	private static final String TAG = HomeActivity.class.getSimpleName();
	
	private ArrayList<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
	private StaggeredGridView sgvThumbnail;
	private ThumbnailAdapter adapter;
	
	private FacebookHelper facebookHelper;
	private FacebookSessionCallback facebookSessionCallback = new FacebookSessionCallback();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_home);
		
		facebookHelper = new FacebookHelper(this, savedInstanceState, facebookSessionCallback);
		
		setupViews();
		requestRecipe();
	}
	
    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.addSessionStatusCallback(facebookSessionCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.removeSessionStatusCallback(facebookSessionCallback);
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
    
	private void setupViews() {
		getSupportActionBar().setIcon(R.drawable.abs_icon);
		
		adapter = new ThumbnailAdapter(this, thumbnails);
		sgvThumbnail = (StaggeredGridView)findViewById(R.id.sgv_thumbnail);
		sgvThumbnail.setColumnCount(2);
		sgvThumbnail.setAdapter(adapter);
		
	}
	
	private void requestRecipe() {
    	new RecipeLoader().execute(WPRestApi.getPostsUrl(50, false));
    }
	
	private class RecipeLoader extends AsyncTask {
		TextView tvError;
		ProgressBar pbLoading; 		
	
		RecipeLoader() {
			tvError = (TextView)findViewById(R.id.tv_error);
			pbLoading = (ProgressBar)findViewById(R.id.pb_loading);			
		}
		
		@Override
		protected Object doInBackground(Object... param) {
			ObjectMapper mapper = new ObjectMapper();
			Posts posts = null;
			
			try {
				posts = mapper.readValue(new URL((String)param[0]), Posts.class);
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
			tvError.setVisibility(View.GONE);
			pbLoading.setVisibility(View.VISIBLE);			
		}
		
		@Override
		protected void onPostExecute(Object result) {			
			pbLoading.setVisibility(View.GONE);			
			
			if (result == null) {
				tvError.setVisibility(View.VISIBLE);
				return;
			} else {
				tvError.setVisibility(View.GONE);
				sgvThumbnail.setVisibility(View.VISIBLE);	
			}

			Posts posts = (Posts)result;
			Log.d(TAG, posts.toString());
			
			for (int i = 0; i < posts.posts.size(); i++) {
				Post post = posts.posts.get(i);
				Thumbnail thumbnail = new Thumbnail(post);
				if (contain(post)) {
					remove(post);					
				}
				
				thumbnails.add(thumbnail);
			}
			
			Collections.sort(thumbnails, new Comparator<Thumbnail>() {
				public int compare(Thumbnail s1, Thumbnail s2) {
					if (s1.getId() > s2.getId()) return -1;
					else if (s1.getId() > s2.getId()) return 1;
					else return 0;
				}
			});
			
			adapter.notifyDataSetChanged();
		}
		
		private void remove(Post post) {
			Iterator<Thumbnail> iter = thumbnails.iterator();
			while (iter.hasNext()) {
				Thumbnail thumbnail = (Thumbnail)iter.next(); 
				if (thumbnail.getId() == post.id) {
					thumbnails.remove(thumbnail);
					return;
				}
			}			
		}
		
		private boolean contain(Post post) {
			Iterator<Thumbnail> iter = thumbnails.iterator();
			while (iter.hasNext()) {
				if (((Thumbnail)iter.next()).getId() == post.id) {
					return true;
				}
			}			
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		return true;		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_login:
        	if (!facebookHelper.isLogin()) {
        		facebookHelper.login();
        	} else {
        		showLogoutDialog();
        	}
        	break;
        }
        return super.onOptionsItemSelected(item);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showLogoutDialog() {
		View innerView = getLayoutInflater().inflate(R.layout.rl_logout, null);
		AlertDialog.Builder builder;
		
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		} else {
			builder = new AlertDialog.Builder(this);
		}
		
		builder.setView(innerView);
		builder.setPositiveButton("확인", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				facebookHelper.logout();
			}
		});
		
		builder.setNegativeButton("취소", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		
		builder.show();
	}
	
    private class FacebookSessionCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            
        }
    }
}
