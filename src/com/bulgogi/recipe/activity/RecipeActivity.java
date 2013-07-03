package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Options;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.CommentAdapter;
import com.bulgogi.recipe.adapter.RecipePagerAdapter;
import com.bulgogi.recipe.auth.FacebookHelper;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.HttpApi;
import com.bulgogi.recipe.http.model.Comment;
import com.bulgogi.recipe.http.model.Like;
import com.bulgogi.recipe.http.model.Post;
import com.facebook.Session;
import com.facebook.SessionState;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viewpagerindicator.CirclePageIndicator;

public class RecipeActivity extends SherlockActivity implements OnClickListener, Session.StatusCallback {
	private static final String TAG = RecipeActivity.class.getSimpleName();

	private PullToRefreshAttacher pullToRefreshAttacher;
	private ListView lvComments;
	private CommentAdapter adapter;
	private ArrayList<Comment> commentList = new ArrayList<Comment>();
	private ArrayList<Like> likeList = new ArrayList<Like>();
	private LinearLayout llHeader;	
	private FacebookHelper facebookHelper;
	private InputMethodManager inputMethodManager;
	private ImageView ivLike;
	private ProgressBar pbLike;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_recipe);
		
		facebookHelper = new FacebookHelper(this, savedInstanceState, this);
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		Intent intent = getIntent();
		Post post = (Post)intent.getSerializableExtra(Extra.POST);		
		setupView(post);
		
		requestComments(post.id);
		requestLike(post.id, false);
	}

	private void setupView(final Post post) {		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(post.title);
		
		pbLike = (ProgressBar)findViewById(R.id.pb_like);
		pbLike.setVisibility(View.GONE);
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		llHeader = (LinearLayout)inflater.inflate(R.layout.ll_recipe_header, null);
		
		final ViewPager pager = (ViewPager)llHeader.findViewById(R.id.pager);
		pager.setAdapter(new RecipePagerAdapter(this, post.getRecipes()));
		pager.setOnTouchListener(new View.OnTouchListener() {
			@Override
	        public boolean onTouch(View v, MotionEvent event) {
	            v.getParent().requestDisallowInterceptTouchEvent(true);
	            return false;
	        }
	    });

	    pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			   pager.getParent().requestDisallowInterceptTouchEvent(true);
			}
	    });
	    
		CirclePageIndicator indicator = (CirclePageIndicator)llHeader.findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		
		TextView tvIngredients = (TextView)llHeader.findViewById(R.id.tv_ingredients);
		String ingredients = post.tags.get(0).ingredients();
		ingredients = ingredients.replaceAll("\\$", ", ");
		tvIngredients.setText(ingredients);

		String contents = post.content.replaceAll("\\<.*?\\>", "");
		TextView tvDirections = (TextView)llHeader.findViewById(R.id.tv_directions);
		contents = contents.substring(contents.indexOf("1."), contents.length());
		String[] directions = contents.split("[1-9]\\.\\s");
		String direction = new String();
		for (int i = 1; i < directions.length; i++) {
			direction += "<span><b>" + i +". " + "</b>" + directions[i].trim() + "</span>";
			if (i != directions.length - 1) {
				direction += "<br/><br/>";
			}
		}
		tvDirections.setText(Html.fromHtml(direction));
		
		ImageView ivYoutube = (ImageView)llHeader.findViewById(R.id.iv_youtube);
		ivYoutube.setOnClickListener(this);
		String youtubeId = post.tags.get(0).youtubeId();
		ivYoutube.setTag(youtubeId);
		if (youtubeId.equals("null")) {
			ivYoutube.setVisibility(View.INVISIBLE);	
		} else {
			ivYoutube.setVisibility(View.VISIBLE);
		}

		final EditText etComment = (EditText)findViewById(R.id.et_comment);
		
		ivLike = (ImageView)findViewById(R.id.iv_like);
		View ivLikeWrapper = findViewById(R.id.ll_like_wrapper);
		ivLikeWrapper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!facebookHelper.isLogin()) {
					showLoginDialog();
				} else {
					final int postId = post.id;
					final String id = facebookHelper.getId();
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							HttpApi httpApi = new HttpApi();
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("post_id", Integer.toString(post.id)));
							params.add(new BasicNameValuePair("fb_id", id));
							
							if (isAlreadyLike(Long.parseLong(id))) {
								httpApi.post("http://14.63.219.181:3000/unlike", params);	
							} else {
								httpApi.post("http://14.63.219.181:3000/like", params);
							}
							
							requestLike(postId, true);
						}
					}).start();
				}
			}
		});
		
		ImageView ivSend = (ImageView)findViewById(R.id.iv_send);
		ivSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!facebookHelper.isLogin()) {
					showLoginDialog();
				} else {
					if (etComment.getText().length() > 0) {
						final int postId = post.id;
						final String id = facebookHelper.getId();
						final String name = facebookHelper.getName();
						final String thumbnail = "http://graph.facebook.com/" + id + "/picture?type=small";
						final String comment = etComment.getText().toString();

						if (id == null || (id != null && id.equals(""))) {
							Toast.makeText(RecipeActivity.this, "로그인 정보를 불러오는데 실패했습니다. 잠시 후에 다시 댓글을 전송해보세요.", Toast.LENGTH_LONG).show();
							return;
						}
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								HttpApi httpApi = new HttpApi();
								List<NameValuePair> params = new ArrayList<NameValuePair>();
								params.add(new BasicNameValuePair("post_id", Integer.toString(postId)));
								params.add(new BasicNameValuePair("fb_id", id));
								params.add(new BasicNameValuePair("user_name", name));
								params.add(new BasicNameValuePair("thumb_url", thumbnail));
								params.add(new BasicNameValuePair("comment", comment));
								httpApi.post("http://14.63.219.181:3000/comment/write", params);
								
								requestComments(postId);
							}
						}).start();
						
						etComment.setText("");
						inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);						
					}
				}
			}
		});
		
		adapter = new CommentAdapter(this, commentList);
		lvComments = (ListView)findViewById(R.id.lv_comments);
		lvComments.addHeaderView(llHeader);		
		lvComments.setAdapter(adapter);
		
		Options options = new Options();
		options.refreshScrollDistance = 0.4f;		
		pullToRefreshAttacher = new PullToRefreshAttacher(this, options);
		pullToRefreshAttacher.setRefreshableView(lvComments, new PullToRefreshAttacher.OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				requestComments(post.id);
				requestLike(post.id, false);
			}
		});
	}

	private void requestComments(int postId) {
		new CommentsLoader().execute("http://14.63.219.181:3000/comment/load/" + postId);
	}
	
	private void requestLike(int postId, boolean showToast) {
		new LikeLoader().execute("http://14.63.219.181:3000/like/" + postId, showToast);
	}
	
	private boolean isAlreadyLike(long facebookId) {
		Iterator<Like> iter = likeList.iterator();
		while (iter.hasNext()) {
			if (((Like)iter.next()).facebookId == facebookId) {
				return true;
			}
		}			
		return false;
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
	public boolean onCreateOptionsMenu(Menu menu) {
//		getSupportMenuInflater().inflate(R.menu.recipe, menu);
		return true;		
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_youtube:
			StringBuilder sb = new StringBuilder("vnd.youtube:"); 
			sb.append(v.getTag());
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
			break;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showLoginDialog() {
		AlertDialog.Builder builder;		
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		} else {
			builder = new AlertDialog.Builder(this);
		}
		
		builder.setMessage(R.string.login_guide);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
	        	if (!facebookHelper.isLogin()) {
	        		facebookHelper.login();
	        	} 
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
	
	private class CommentsLoader extends AsyncTask {				
		private TextView tvCountComment = (TextView)llHeader.findViewById(R.id.tv_count_comment);
		private LinearLayout llEmpty = (LinearLayout)llHeader.findViewById(R.id.ll_empty_coments);
		private LinearLayout llLoadingMsg = (LinearLayout)llHeader.findViewById(R.id.ll_loading_msg);
		private TextView tvLoadingMsg = (TextView)llLoadingMsg.getChildAt(1);				
		
		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Comment> comments = null;
			
			try {
				comments = mapper.readValue(new URL((String)params[0]), new TypeReference<List<Comment>>(){});
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();				
			} catch (UnknownHostException e) { 				
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
						
			return comments;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			pullToRefreshAttacher.setRefreshComplete();
			
			if (result == null) {
				llLoadingMsg.setVisibility(View.VISIBLE);
				tvLoadingMsg.setText(R.string.loading_error);
				return;
			} else {
				llLoadingMsg.setVisibility(View.GONE);
				tvLoadingMsg.setText(R.string.loading_comments);
			}
			
			ArrayList<Comment> comments = (ArrayList<Comment>)result;
			Log.d(TAG, comments.toString());
								
			int length = comments.size(); 
			if (length == 0) {
				llEmpty.setVisibility(View.VISIBLE);
			} else {
				llEmpty.setVisibility(View.GONE);
			}
			
			for (int i = 0; i < length; i++) {
				Comment comment = (Comment)comments.get(i);
				if (!contains(comment)) {
					commentList.add(comment);
				}
			}

			tvCountComment.setText(Integer.toString(length));
			adapter.notifyDataSetChanged();
		}
		
		private boolean contains(Comment comment) {
			Iterator<Comment> iter = commentList.iterator();
			while (iter.hasNext()) {
				if (((Comment)iter.next()).commentId == comment.commentId) {
					return true;
				}
			}			
			return false;
		}
	}
	
	private class LikeLoader extends AsyncTask {				
		private TextView tvLike = (TextView)llHeader.findViewById(R.id.tv_count_like); 
		private Boolean showToast;
		
		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Like> likes = new ArrayList<Like>();
			showToast = (Boolean)params[1]; 
					
			try {
				likes = mapper.readValue(new URL((String)params[0]), new TypeReference<List<Like>>(){});
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();				
			} catch (UnknownHostException e) { 				
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
						
			return likes;
		}
		
		@Override
        protected void onPreExecute() {
			super.onPreExecute();
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					pbLike.setVisibility(View.VISIBLE);
					ivLike.setVisibility(View.GONE);
				}
			});			
		}
		
		@Override
		protected void onPostExecute(Object result) {
			pbLike.setVisibility(View.GONE);
			ivLike.setVisibility(View.VISIBLE);
			
			likeList = (ArrayList<Like>)result;
			tvLike.setText(Integer.toString(likeList.size()));
			
			if (likeList.size() > 0 && facebookHelper.getId() != null) {
				if (isAlreadyLike(Long.parseLong(facebookHelper.getId()))) {
					if (showToast) {
						Toast.makeText(RecipeActivity.this, R.string.like_msg, Toast.LENGTH_SHORT).show();
					}
					ivLike.setImageResource(R.drawable.btn_unlike);
					
				} else {
					if (showToast) {
						Toast.makeText(RecipeActivity.this, R.string.unlike_msg, Toast.LENGTH_SHORT).show();
					}
					ivLike.setImageResource(R.drawable.btn_like);
				}
			} else {
				if (showToast) {
					Toast.makeText(RecipeActivity.this, R.string.unlike_msg, Toast.LENGTH_SHORT).show();
				}
				ivLike.setImageResource(R.drawable.btn_like);
			}
		}		
	}
}
