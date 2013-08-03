package com.bulgogi.recipe.activity;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.BlogSearchAdapter;
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.NaverRestApi;
import com.bulgogi.recipe.http.model.Blog;
import com.bulgogi.recipe.http.model.Blog.Item;
import com.bulgogi.recipe.parser.BlogSearchXmlParser;
import com.bulgogi.recipe.parser.BlogSearchXmlParser.OnParserListener;

public class BlogListActivity extends SherlockListActivity {
	private ArrayList<Item> items = new ArrayList<Item>();
	private BlogSearchAdapter adapter;
	private Blog blog;
	private boolean isLoading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.setContentView(R.layout.ac_blog_search);
		
		adapter = new BlogSearchAdapter(this, items);
		setListAdapter(adapter);

		Intent intent = getIntent();
		blog = (Blog) intent.getSerializableExtra(Extra.BLOG_SEARCH_RESULT);
		String title = intent.getStringExtra(Extra.BLOG_SEARCH_RESULT_TITLE); 
		
		setupViews(title);
		
		request(blog.query, Integer.parseInt(blog.display), Integer.parseInt(blog.start));
	}

	private void setupViews(String title) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(title);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		final ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(BlogListActivity.this, WebViewActivity.class);
				intent.putExtra(Extra.WEBVIEW_URL, items.get(position).link);
				intent.putExtra(Extra.WEBVIEW_TITLE, items.get(position).bloggername);
				startActivity(intent);
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener(){
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (listView.getLastVisiblePosition() == (adapter.getCount() - 1) && !isLoading) {
					int start = Integer.parseInt(blog.start) + Integer.parseInt(blog.display);
					if (Integer.parseInt(blog.total) >= start) {
						request(blog.query, Constants.NAVER_BLOG_QUERY_COUNT, start);
					}
				}
			}
			
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {}
		});
	}

	private void request(final String query, int display, int start) {
		setSupportProgressBarIndeterminateVisibility(true);
		isLoading = true;
		
		BlogSearchXmlParser parser = new BlogSearchXmlParser();
		parser.setOnParserListener(new OnParserListener() {
			@Override
			public void onComplete(Blog b) {
				setSupportProgressBarIndeterminateVisibility(false);
				isLoading = false;
				
				if (b != null && b.start != null && b.total != null) {
					blog = b;
					blog.query = query;
					items.addAll(blog.items);
					adapter.notifyDataSetChanged();
				}
			}
		});
		
		try {
			parser.read(NaverRestApi.getBlogSearchUrl(query, display, start));
		} catch (XmlPullParserException e) {
			setSupportProgressBarIndeterminateVisibility(false);
			isLoading = false;
			e.printStackTrace();
		} catch (IOException e) {
			setSupportProgressBarIndeterminateVisibility(false);
			isLoading = false;
			e.printStackTrace();
		}
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
