package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ScrollViewDelegate;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
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
import com.facebook.widget.ProfilePictureView;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HomeActivity extends SherlockActivity implements Session.StatusCallback {
	private static final String TAG = HomeActivity.class.getSimpleName();
	
	private ArrayList<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
	private GridView gvThumbnail;
	private ThumbnailAdapter adapter;
	private PullToRefreshAttacher pullToRefreshAttacher;	
	private FacebookHelper facebookHelper;
	private MenuItem actionbarLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_home);
		
		facebookHelper = new FacebookHelper(this, savedInstanceState, this);		
	    
		setupViews();
		requestRecipe();
	}
    
	private void setupViews() {
		getSupportActionBar().setIcon(R.drawable.abs_icon);
		
		adapter = new ThumbnailAdapter(this, thumbnails);
		gvThumbnail = (GridView)findViewById(R.id.sgv_thumbnail);
		gvThumbnail.setNumColumns(2);
		gvThumbnail.setAdapter(adapter);
	
		pullToRefreshAttacher = new PullToRefreshAttacher(this);
		pullToRefreshAttacher.setRefreshableView(gvThumbnail, (PullToRefreshAttacher.ViewDelegate)new ScrollViewDelegate(), 
				new PullToRefreshAttacher.OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				requestRecipe();		
			}
		});
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
    
	private void requestRecipe() {
    	new RecipeLoader().execute(WPRestApi.getPostsUrl(50, false));
    }
	
	private class RecipeLoader extends AsyncTask {
		TextView tvError = (TextView)findViewById(R.id.tv_error);
		ProgressBar pbLoading = (ProgressBar)findViewById(R.id.pb_loading);
		
		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			Posts posts = null;
			
			try {
				posts = mapper.readValue(new URL((String)params[0]), Posts.class);
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
			pullToRefreshAttacher.setRefreshComplete();
			
			if (result == null) {
				tvError.setVisibility(View.VISIBLE);
				return;
			} else {
				tvError.setVisibility(View.GONE);
				gvThumbnail.setVisibility(View.VISIBLE);	
			}

			Posts posts = (Posts)result;
			Log.d(TAG, posts.toString());
			
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
		
		private boolean contains(Post post) {
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
		actionbarLogin = menu.findItem(R.id.action_login);
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
        }
        return super.onOptionsItemSelected(item);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showLogoutDialog() {
		View innerView = getLayoutInflater().inflate(R.layout.rl_logout, null);
		
		if (facebookHelper.getId() != null) {
			ProfilePictureView ppvProfile = (ProfilePictureView)innerView.findViewById(R.id.ppv_profile);
			ppvProfile.setProfileId(facebookHelper.getId());
		}
		
		if (facebookHelper.getName() != null) {
			TextView tvName = (TextView)innerView.findViewById(R.id.tv_name);
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
			public void onClick(DialogInterface arg0, int arg1) {}
		});
		
		builder.show();
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
    		facebookHelper.makeMeRequest(session);
		}
	}	
}
