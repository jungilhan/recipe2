package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
import com.actionbarsherlock.internal.widget.IcsLinearLayout;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.ThumbnailAdapter;
import com.bulgogi.recipe.application.RecipeApplication;
import com.bulgogi.recipe.auth.FacebookHelper;
import com.bulgogi.recipe.auth.FacebookHelper.OnSessionListener;
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.http.NodeRestApi;
import com.bulgogi.recipe.http.WPRestApi;
import com.bulgogi.recipe.http.model.Count;
import com.bulgogi.recipe.http.model.Post;
import com.bulgogi.recipe.http.model.Posts;
import com.bulgogi.recipe.model.Thumbnail;
import com.bulgogi.recipe.utils.KakaoLink;
import com.bulgogi.recipe.utils.PreferenceHelper;
import com.bulgogi.recipe.utils.Sort;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.ProfilePictureView;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.localytics.android.LocalyticsSession;
import com.nbpcorp.mobilead.sdk.MobileAdListener;
import com.nbpcorp.mobilead.sdk.MobileAdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;

public class HomeActivity extends SherlockActivity implements Session.StatusCallback, ActionBar.OnNavigationListener {
	private static final String TAG = HomeActivity.class.getSimpleName();

	private LocalyticsSession localyticsSession;

	private ArrayList<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
	private HashMap<Integer, Count> countMap = new HashMap<Integer, Count>();
	private PullToRefreshGridView gvRefreshWrapper;
	private GridView gvThumbnail;
	private ThumbnailAdapter adapter;
	private FacebookHelper facebookHelper;
	private DrawerLayout drawerLayout;
	private SherlockActionBarDrawerToggle drawerToggle;
	private IcsSpinner spinner;
	private MobileAdView adView;
	private boolean isLoading = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_home);

		facebookHelper = new FacebookHelper(this, savedInstanceState, this);
		facebookHelper.setOnLoginListener(new OnSessionListener() {
			@Override
			public void onLoginComplete(String id, String name) {
				ProfilePictureView ppvProfile = (ProfilePictureView) findViewById(R.id.ppv_profile);
				ppvProfile.setProfileId(id);
				
				TextView tvName = (TextView) findViewById(R.id.tv_name);
				tvName.setText(name);
				
				setProfileVisibility(true);
			}

			@Override
			public void onLogoutComplete() {
				setProfileVisibility(false);
			}
		});
		
		setupViews();
		requestCountInfo(false);
		requestRecipe(Constants.WP_RECIPE_QUERY_COUNT, true);

		localyticsSession = new LocalyticsSession(this);
		localyticsSession.open();
		localyticsSession.upload();
	}

	private void setupViews() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.abs_icon);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
 
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerLayout.setDrawerListener(new DrawerListener() {
			@Override
			public void onDrawerOpened(View drawerView) {
				drawerToggle.onDrawerOpened(drawerView);
				
				boolean isLogin = facebookHelper.isLogin();
				if (isLogin) {
					ProfilePictureView ppvProfile = (ProfilePictureView) findViewById(R.id.ppv_profile);
					ppvProfile.setProfileId(facebookHelper.getId());
					
					TextView tvName = (TextView) findViewById(R.id.tv_name);
					tvName.setText(facebookHelper.getName());
				}
				
				setProfileVisibility(isLogin);
			}
			
			@Override
			public void onDrawerClosed(View drawerView) {
				drawerToggle.onDrawerClosed(drawerView);
			}
			
			@Override
			public void onDrawerStateChanged(int state) {
				drawerToggle.onDrawerStateChanged(state);
			}
			
			@Override
			public void onDrawerSlide(View drawerView, float offset) {
				drawerToggle.onDrawerSlide(drawerView, offset);
			}
		});
		
		drawerToggle = new SherlockActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_dark, R.string.drawer_open, R.string.drawer_close);
		drawerToggle.syncState();
		
		Context context = actionBar.getThemedContext(); 
		String[] items = getResources().getStringArray(R.array.ar_sort);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, R.layout.tv_sherlock_spinner_item, items); 
		spinnerAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item); 

		spinner = new IcsSpinner(this, null, R.attr.actionDropDownStyle); 
		spinner.setAdapter(spinnerAdapter);
		spinner.setSelection(PreferenceHelper.getInstance().getInt(Constants.PREF_SORT_TYPE, 0));
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() { 
		    @Override 
		    public void onItemSelected(IcsAdapterView<?> parent, View view, int position, long id) {
		    	PreferenceHelper.getInstance().putInt(Constants.PREF_SORT_TYPE, position);
		    	sortThumbnail(thumbnails, position, true);
		    }
		    
		    @Override 
		    public void onNothingSelected(IcsAdapterView<?> parent) {
		    	
		    }
		}); 

		IcsLinearLayout listNavLayout = (IcsLinearLayout) getLayoutInflater().inflate(R.layout.abs__action_bar_tab_bar_view, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		params.rightMargin = 6;
		listNavLayout.addView(spinner, params);
		listNavLayout.setGravity(Gravity.RIGHT);

		actionBar.setCustomView(listNavLayout, new ActionBar.LayoutParams(Gravity.RIGHT));
		actionBar.setDisplayShowCustomEnabled(true);
        
		adapter = new ThumbnailAdapter(this, thumbnails);
		gvRefreshWrapper = (PullToRefreshGridView) findViewById(R.id.sgv_thumbnail);
		gvThumbnail = gvRefreshWrapper.getRefreshableView();

		gvRefreshWrapper.setOnRefreshListener(new OnRefreshListener<GridView>() {
		    @Override
		    public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				requestCountInfo(false);
				requestRecipe(Constants.WP_RECIPE_QUERY_COUNT, false);
		    }
		});
		
		int columns = RecipeApplication.isTablet() == true ? Constants.GRIDVIEW_TABLET_COLUMNS : Constants.GRIDVIEW_DEFAULT_COLUMNS;
		gvThumbnail.setNumColumns(columns);
		gvThumbnail.setAdapter(adapter);

        View llProfile = findViewById(R.id.ll_profile);
        View llLogin = findViewById(R.id.ll_login);
        llProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLogoutDialog();
			}
		});
        
        llLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				facebookHelper.login();
			}
		});

        if (facebookHelper.getId() != null) {
			ProfilePictureView ppvProfile = (ProfilePictureView) findViewById(R.id.ppv_profile);
			ppvProfile.setProfileId(facebookHelper.getId());
		}

        boolean isLogin = facebookHelper.isLogin();
        setProfileVisibility(isLogin);
        if (isLogin) {
        	ProfilePictureView ppvProfile = (ProfilePictureView) findViewById(R.id.ppv_profile);
			ppvProfile.setProfileId(facebookHelper.getId());
			
			TextView tvName = (TextView) findViewById(R.id.tv_name);
			tvName.setText(facebookHelper.getName());
        }
        
        View llClearCache = findViewById(R.id.ll_clear_image_cache);
        llClearCache.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().clearDiscCache();
				
				Toast.makeText(HomeActivity.this, "캐시가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
			}
		});
        
        View llShareViewKakaoTalk = findViewById(R.id.ll_share_via_kakaotalk);
        llShareViewKakaoTalk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				KakaoLink kakaoLink = KakaoLink.getLink(getApplicationContext());
				if (!kakaoLink.isAvailableIntent())
				  return;
				
				try {
					kakaoLink.openKakaoLink(
							HomeActivity.this,
							"https://play.google.com/store/apps/details?id=com.bulgogi.recipe",
							"진격의 야간매점 레시피! 해피투게더 야간매점에서 소개하는 다양한 레시피로 홈메이드 스타일 푸드를 즐겨보세요.",
							getPackageName(),
							getPackageManager().getPackageInfo(getPackageName(), 0).versionName, "야간매점 레시피", "UTF-8");
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
        
		adView = (MobileAdView) findViewById(R.id.adview);
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
		
		requestCountInfo(true);
	}

	@Override
	protected void onPause() {
		localyticsSession.close();
		localyticsSession.upload();
		super.onPause();

		gvRefreshWrapper.onRefreshComplete();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		adView.destroy();
		adView = null;
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

	private void requestCountInfo(boolean invalidate) {
		new CountInfoLoader().execute(NodeRestApi.getCountInfoUrl(), invalidate);
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
			gvRefreshWrapper.onRefreshComplete();

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
			if (Constants.Config.DEBUG) {
				Log.d(TAG, posts.toString());
			}

			for (int i = 0; i < posts.posts.size(); i++) {
				Post post = posts.posts.get(i);
				Thumbnail thumbnail = new Thumbnail(post);
				if (contains(post)) {
					remove(post);
				}

				thumbnails.add(thumbnail);
			}

			updateCountInfo(false);
			
			int type = spinner.getSelectedItemPosition();
			sortThumbnail(thumbnails, type, true);

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
		private boolean invalidate = false;
		
		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Count> counts = null;
			invalidate = (Boolean) params[1];
					
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

			updateCountInfo(invalidate);

			// [XXX] 갱신된 영역만 처리 필요
			//if (invalidate && thumbnails.size() > 0) {
			//	adapter.notifyDataSetChanged();
			//}
		}
	}

	private void updateCountInfo(boolean invalidate) {
		for (int i = 0; i < thumbnails.size(); i++) {
			Thumbnail thumbnail = thumbnails.get(i);
			int postId = thumbnail.getId();
			Count count = countMap.get(postId);
			if (count != null) {
				if (thumbnail.getCommentCount() != count.comments) {
					thumbnail.setCommentCount(count.comments);
					if (invalidate) {
						updateItemAtPosition(i++);
					}
				}
				
				if (thumbnail.getLikeCount() != count.likes) {
					thumbnail.setLikeCount(count.likes);
					if (invalidate) {
						updateItemAtPosition(i++);
					}
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
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
		requestCountInfo(false);
		requestRecipe(Constants.WP_RECIPE_QUERY_COUNT, true);
	}
	
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }
    
    private void updateItemAtPosition(int position) {
	    int visiblePosition = gvThumbnail.getFirstVisiblePosition();
	    View view = gvThumbnail.getChildAt(position - visiblePosition);
	    gvThumbnail.getAdapter().getView(position, view, gvThumbnail);
	}
    
    private void sortThumbnail(ArrayList<Thumbnail> thumbnails, int type, boolean notify) {
    	if (thumbnails.size() <= 0) {
    		return;
    	}

    	Sort.byNewest(thumbnails);
    	
    	switch (type) {
    	case Constants.SORT_BY_NEWEST:
    		break;
    	case Constants.SORT_BY_VIEW_COUNT:
    		Sort.byViewCount(thumbnails, countMap);
    		break;
    	case Constants.SORT_BY_LIKE_COUNT:
    		Sort.byLikeCount(thumbnails, countMap);
    		break;
    	case Constants.SORT_BY_COMMENT_COUNT:
    		Sort.byCommentCount(thumbnails, countMap);
    		break;
    	}
    	
    	if (notify) {
    		adapter.notifyDataSetChanged();
    	}
    }
    
    private void setProfileVisibility(boolean visibility) {
    	View llProfile = findViewById(R.id.ll_profile);
        View llLogin = findViewById(R.id.ll_login);
        
    	if (visibility) {
        	llProfile.setVisibility(View.VISIBLE);
        	llLogin.setVisibility(View.GONE);
		} else {
			llProfile.setVisibility(View.GONE);
			llLogin.setVisibility(View.VISIBLE);
		}    	
    }
}
