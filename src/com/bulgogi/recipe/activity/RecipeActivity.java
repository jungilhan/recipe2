package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
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
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.FBRestApi;
import com.bulgogi.recipe.http.HttpApi;
import com.bulgogi.recipe.http.NaverRestApi;
import com.bulgogi.recipe.http.NodeRestApi;
import com.bulgogi.recipe.http.model.Blog;
import com.bulgogi.recipe.http.model.Comment;
import com.bulgogi.recipe.http.model.Like;
import com.bulgogi.recipe.http.model.Post;
import com.bulgogi.recipe.parser.BlogSearchXmlParser;
import com.bulgogi.recipe.parser.BlogSearchXmlParser.OnParserListener;
import com.facebook.Session;
import com.facebook.SessionState;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nbpcorp.mobilead.sdk.MobileAdListener;
import com.nbpcorp.mobilead.sdk.MobileAdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.viewpagerindicator.CirclePageIndicator;

public class RecipeActivity extends SherlockActivity implements OnClickListener, Session.StatusCallback {
	private static final String TAG = RecipeActivity.class.getSimpleName();

	private PullToRefreshListView lvRefreshWrapper;
	private ListView lvComments;
	private CommentAdapter adapter;
	private ArrayList<Comment> commentList = new ArrayList<Comment>();
	private ArrayList<Like> likeList = new ArrayList<Like>();
	private LinearLayout llHeader;
	private FacebookHelper facebookHelper;
	private InputMethodManager inputMethodManager;
	private LinearLayout lvLikeButtonWrapper;
	private LinearLayout llLikeUsersWrapper;
	TextView tvBlogSearch;
	private boolean isLoading = false;
	private Post post;
	private Blog blog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_recipe);

		facebookHelper = new FacebookHelper(this, savedInstanceState, this);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		Intent intent = getIntent();
		post = (Post) intent.getSerializableExtra(Extra.POST);
		setupViews(post);

		requestComments(post.id);
		requestLike(post.id, false);
		postPageView(post.id);
		try {
			requestBlogSearch(post.title, post.tags.get(0).chef(), Constants.NAVER_BLOG_QUERY_COUNT, 1);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Constants.Config.DEBUG) {
			Toast.makeText(this, "Post Id: " + post.id, Toast.LENGTH_SHORT).show();
		}
	}

	private void setupViews(final Post post) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(post.title);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		llHeader = (LinearLayout) inflater.inflate(R.layout.ll_recipe_header, null);

		final ViewPager pager = (ViewPager) llHeader.findViewById(R.id.pager);
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
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				pager.getParent().requestDisallowInterceptTouchEvent(true);
			}
		});

		CirclePageIndicator indicator = (CirclePageIndicator) llHeader.findViewById(R.id.indicator);
		indicator.setViewPager(pager);

		TextView tvIngredients = (TextView) llHeader.findViewById(R.id.tv_ingredients);
		String ingredients = post.tags.get(0).ingredients();
		ingredients = ingredients.replaceAll("\\$", ", ");
		tvIngredients.setText(ingredients);

		String contents = post.content.replaceAll("\\<.*?\\>", "");
		TextView tvDirections = (TextView) llHeader.findViewById(R.id.tv_directions);
		contents = contents.substring(contents.indexOf("1."), contents.length());
		String[] directions = contents.split("[1-9]\\.\\s");
		String direction = new String();
		for (int i = 1; i < directions.length; i++) {
			direction += "<span><b>" + i + ". " + "</b>" + directions[i].trim() + "</span>";
			if (i != directions.length - 1) {
				direction += "<br/><br/>";
			}
		}
		tvDirections.setText(Html.fromHtml(direction));

		final ImageView ivYoutube = (ImageView) llHeader.findViewById(R.id.iv_youtube);
		ivYoutube.setOnClickListener(this);
		ivYoutube.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Drawable drawable = ivYoutube.getDrawable();
				if (drawable == null) {
					return false;
				}

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					drawable.setColorFilter(0x22000000, PorterDuff.Mode.SRC_ATOP);
					ivYoutube.invalidate();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					drawable.clearColorFilter();
					ivYoutube.invalidate();
					break;
				}
				return false;
			}
		});
		String youtubeId = post.tags.get(0).youtubeId();
		ivYoutube.setTag(youtubeId);
		if (youtubeId.equals("null")) {
			ivYoutube.setVisibility(View.INVISIBLE);
		} else {
			ivYoutube.setVisibility(View.VISIBLE);
		}		

		final EditText etComment = (EditText) findViewById(R.id.et_comment);

		lvLikeButtonWrapper = (LinearLayout) findViewById(R.id.ll_like_wrapper);
		lvLikeButtonWrapper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!facebookHelper.isLogin()) {
					showLoginDialog();
				} else {
					lvLikeButtonWrapper.setVisibility(View.GONE);

					final int postId = post.id;
					String id = facebookHelper.getId();
					HttpApi httpApi = new HttpApi();
					RequestParams params = new RequestParams();
					params.put("post_id", Integer.toString(post.id));
					params.put("fb_id", id);

					if (isAlreadyLike(Long.parseLong(id))) {
						httpApi.post(NodeRestApi.getPostUnLikeUrl(), params, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								requestLike(postId, true);
							}
						});
					} else {
						httpApi.post(NodeRestApi.getPostLikeUrl(), params, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								requestLike(postId, true);
							}
						});
					}
				}
			}
		});

		ImageView ivSend = (ImageView) findViewById(R.id.iv_send);
		ivSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!facebookHelper.isLogin()) {
					showLoginDialog();
				} else {
					if (etComment.getText().length() > 0) {
						final int postId = post.id;
						String id = facebookHelper.getId();						
						if (id == null || (id != null && id.equals(""))) {
							Toast.makeText(RecipeActivity.this, getResources().getString(R.string.comment_send_error), Toast.LENGTH_LONG).show();
							return;
						}
						
						String name = facebookHelper.getName();
						String thumbnail = FBRestApi.getProfileUrl(Long.parseLong(id));
						String comment = etComment.getText().toString();

						HttpApi httpApi = new HttpApi();
						RequestParams params = new RequestParams();
						params.put("post_id", Integer.toString(postId));
						params.put("fb_id", id);
						params.put("user_name", name);
						params.put("thumb_url", thumbnail);
						params.put("comment", comment);
						httpApi.post(NodeRestApi.getPostCommentUrl(), params, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response) {
								requestComments(postId);
							}
						});

						etComment.setText("");
						inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					}
				}
			}
		});

		adapter = new CommentAdapter(this, commentList);
		
		lvRefreshWrapper = (PullToRefreshListView) findViewById(R.id.lv_comments);
		lvRefreshWrapper.setOnRefreshListener(new OnRefreshListener<ListView>() {
		    @Override
		    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		    	requestComments(post.id);
				requestLike(post.id, false);
		    }
		});	
		
		lvComments = (ListView) lvRefreshWrapper.getRefreshableView(); 
		lvComments.addHeaderView(llHeader);
		lvComments.setAdapter(adapter);
		
		LinearLayout llBlogSearchWrapper = (LinearLayout) findViewById(R.id.ll_blog_search_wrapper);
		llBlogSearchWrapper.setOnClickListener(this);
		tvBlogSearch = (TextView) findViewById(R.id.tv_blog_search);
		
		llLikeUsersWrapper = (LinearLayout) findViewById(R.id.ll_like_users_wrapper);
		llLikeUsersWrapper.setOnClickListener(this);
		
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

	private void requestComments(int postId) {
		if (isLoading) {
			return;
		} else {
			isLoading = true;
		}

		new CommentsLoader().execute(NodeRestApi.getCommentsUrl(postId));
	}

	private void requestLike(int postId, boolean showToast) {
		new LikeLoader().execute(NodeRestApi.getLikeUsersUrl(postId), showToast);
	}
	
	private void requestBlogSearch(String title, String chef, int display, int start) throws XmlPullParserException, IOException {
		final String query = URLEncoder.encode(title + " " + chef + " 야간매점 레시피", "UTF-8");
		
		BlogSearchXmlParser parser = new BlogSearchXmlParser();
		parser.setOnParserListener(new OnParserListener() {
			@Override
			public void onComplete(Blog b) {
				if (b != null && b.total != null) {
					blog = b;
					blog.query = query;
					tvBlogSearch.setText(Html.fromHtml("<b>" + blog.total + "</b>" + "개의 블로그 글이 검색되었습니다."));
				}
			}
		});
		
		parser.read(NaverRestApi.getBlogSearchUrl(query, display, start));
	}

	private void postPageView(int postId) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(NodeRestApi.getPostPageViewUrl(postId), null);
	}

	private boolean isAlreadyLike(long facebookId) {
		Iterator<Like> iter = likeList.iterator();
		while (iter.hasNext()) {
			if (((Like) iter.next()).facebookId == facebookId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isLoading() {
		return isLoading;
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
	protected void onPause() {
		super.onPause();

		lvRefreshWrapper.onRefreshComplete();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.recipe, menu);
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
		Intent intent;
		
		switch (v.getId()) {
		case R.id.iv_youtube:
			try {
				StringBuilder sb = new StringBuilder("vnd.youtube:");
				sb.append(v.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
			} catch (ActivityNotFoundException e) {
				StringBuilder sb = new StringBuilder("http://www.youtube.com/watch?v=");
				sb.append(v.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
			}
			break;
		case R.id.ll_blog_search_wrapper:
			if (blog != null) {
				intent = new Intent(this, BlogListActivity.class);
				intent.putExtra(Extra.BLOG_SEARCH_RESULT, blog);
				intent.putExtra(Extra.BLOG_SEARCH_RESULT_TITLE, "\"" + post.title + "\"" + getResources().getString(R.string.blog_search_title));
				startActivity(intent);
			} else {
				Toast.makeText(this, "블로그 검색 결과를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ll_like_users_wrapper:
			intent = new Intent(this, LikeUserListActivity.class);
			intent.putExtra(Extra.LIKE_USERS, (Serializable) likeList);
			startActivity(intent);
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

	private void showLikeUser(ArrayList<Like> likes) {
		if (likes.size() == 0) {
			llLikeUsersWrapper.setVisibility(View.GONE);
			return;
		}
		
		llLikeUsersWrapper.setVisibility(View.VISIBLE);
		
		TextView tvLikeUsers = (TextView) findViewById(R.id.tv_like_users);
		tvLikeUsers.setText(likes.size() + "명이 좋아해요");
		
		LinearLayout llLikeUsers = (LinearLayout) findViewById(R.id.ll_like_users);
		llLikeUsers.removeAllViews();
		
		int width = getResources().getDimensionPixelSize(R.dimen.profile_width);
		int height = getResources().getDimensionPixelSize(R.dimen.profile_height);
		int margin = getResources().getDimensionPixelSize(R.dimen.profile_margin);
		int round = getResources().getDimensionPixelSize(R.dimen.profile_round);
		int layoutPadding = getResources().getDimensionPixelSize(R.dimen.like_users_padding);
		
		ImageLoader imageLoader = ImageLoader.getInstance();
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.resetViewBeforeLoading(true)
			.showImageForEmptyUri(R.drawable.ic_blank_profile)
			.showImageOnFail(R.drawable.ic_blank_profile)
			.showStubImage(R.drawable.ic_blank_profile)
			.bitmapConfig(Config.RGB_565)
			.displayer(new RoundedBitmapDisplayer(round))
			.build();

		// [XXX] -1는 +N 정보를 표시하기 위한 영역
		int max = (getResources().getDisplayMetrics().widthPixels - (layoutPadding * 2)) / (width + margin) - 1;
		int length = 0;
		boolean isOverflow = likes.size() > max ? true : false;		
		if (isOverflow) {
			length = max;
		} else {
			length = likes.size();
		}
		
		for (int i = 0; i < length; i++) {
			ImageView ivUser = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			params.leftMargin = margin;
			
			if (i == length - 1) {
				params.rightMargin = margin;
			}
			
			ivUser.setLayoutParams(params);
			ivUser.setScaleType(ScaleType.CENTER_CROP);
			llLikeUsers.addView(ivUser);
			
			Like like = likes.get(i);	
			imageLoader.displayImage(FBRestApi.getProfileUrl(like.facebookId), ivUser, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					Animation animation = AnimationUtils.loadAnimation(RecipeActivity.this, R.anim.fade_in);
					view.setAnimation(animation);
					animation.start();
				}
			});
		}
		
		if (isOverflow) {
			TextView tvMore = new TextView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			tvMore.setLayoutParams(params);			
			tvMore.setTextColor(Color.WHITE);
			tvMore.setGravity(Gravity.CENTER);
			tvMore.setTypeface(null, Typeface.BOLD);
			tvMore.setBackgroundResource(R.drawable.round_rect_more);
			
			int more = likes.size() - max;
			tvMore.setText("+" + more);
			
			llLikeUsers.addView(tvMore);
		}
		
	}
	
	private class CommentsLoader extends AsyncTask {
		private TextView tvCountComment = (TextView) llHeader.findViewById(R.id.tv_count_comment);
		private LinearLayout llLoadingMsg = (LinearLayout) llHeader.findViewById(R.id.ll_loading_msg);
		private TextView tvLoadingMsg = (TextView) llLoadingMsg.getChildAt(1);

		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Comment> comments = null;

			try {
				comments = mapper.readValue(new URL((String) params[0]), new TypeReference<List<Comment>>() {
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

			return comments;
		}

		@Override
		protected void onPostExecute(Object result) {			
			lvRefreshWrapper.onRefreshComplete();

			isLoading = false;
			findViewById(R.id.pb_main_loading).setVisibility(View.GONE);

			if (result == null) {
				llLoadingMsg.setVisibility(View.VISIBLE);
				tvLoadingMsg.setText(R.string.loading_error);
				return;
			} else {
				llLoadingMsg.setVisibility(View.GONE);
				tvLoadingMsg.setText(R.string.loading_comments);
			}

			ArrayList<Comment> comments = (ArrayList<Comment>) result;
			if (Constants.Config.DEBUG) {
				Log.d(TAG, comments.toString());
			}

			int length = comments.size();
			for (int i = 0; i < length; i++) {
				Comment comment = (Comment) comments.get(i);
				if (!contains(comment)) {
					commentList.add(comment);
				}
			}

			tvCountComment.setText(Integer.toString(length));
			
			Collections.sort(commentList, new Comparator<Comment>() {
				public int compare(Comment s1, Comment s2) {
					if (s1.commentId > s2.commentId) {
						return 1;
					} else if (s1.commentId < s2.commentId) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			
			adapter.notifyDataSetChanged();
		}

		private boolean contains(Comment comment) {
			Iterator<Comment> iter = commentList.iterator();
			while (iter.hasNext()) {
				if (((Comment) iter.next()).commentId == comment.commentId) {
					return true;
				}
			}
			return false;
		}
	}

	private class LikeLoader extends AsyncTask {
		private TextView tvLike = (TextView) llHeader.findViewById(R.id.tv_count_like);
		private ProgressBar pbLike = (ProgressBar) findViewById(R.id.pb_like);
		private Boolean showToast = false;

		@Override
		protected Object doInBackground(Object... params) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Like> likes = new ArrayList<Like>();
			showToast = (Boolean) params[1];

			try {
				likes = mapper.readValue(new URL((String) params[0]), new TypeReference<List<Like>>() {
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

			return likes;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					pbLike.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		protected void onPostExecute(Object result) {
			lvLikeButtonWrapper.setVisibility(View.VISIBLE);
			pbLike.setVisibility(View.GONE);

			likeList = (ArrayList<Like>) result;
			Collections.sort(likeList, new Comparator<Like>() {
				public int compare(Like s1, Like s2) {
					if (s1.likeId > s2.likeId) {
						return -1;
					} else if (s1.likeId < s2.likeId) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			
			tvLike.setText(Integer.toString(likeList.size()));
			showLikeUser(likeList);

			ImageView ivLike = ((ImageView) lvLikeButtonWrapper.getChildAt(0));
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
