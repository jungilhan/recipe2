package com.bulgogi.recipe.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.CommentAdapter;
import com.bulgogi.recipe.adapter.RecipePagerAdapter;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.model.Post;
import com.bulgogi.recipe.model.Comment;
import com.viewpagerindicator.CirclePageIndicator;

public class RecipeActivity extends SherlockActivity {
	private ListView lvComments;
	private CommentAdapter adapter;
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private LinearLayout header;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_recipe);
		
		Intent intent = getIntent();
		Post post = (Post)intent.getSerializableExtra(Extra.POST);
		
		setupView(post);
	}
	
	private void setupView(Post post) {		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(post.title);
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		header = (LinearLayout)inflater.inflate(R.layout.ll_recipe_header, null);
		
		final ViewPager pager = (ViewPager)header.findViewById(R.id.pager);
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
	    
		CirclePageIndicator indicator = (CirclePageIndicator)header.findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		
		adapter = new CommentAdapter(this, comments);
		lvComments = (ListView)findViewById(R.id.lv_comments);
		lvComments.addHeaderView(header);
		lvComments.setAdapter(adapter);
		
		comments.add(new Comment("drawable://" + R.drawable.ic_blank_profile, "한정일", "우왕 굳! :D"));
		comments.add(new Comment("drawable://" + R.drawable.ic_blank_profile, "오진성", "완전 맛있어요. 많이 먹고 살쩌버려라! ㅋㅋㅋㅋ"));
		comments.add(new Comment("drawable://" + R.drawable.ic_blank_profile, "유인영", "헐, 완전 MSG 범벅이네!"));
		comments.add(new Comment("drawable://" + R.drawable.ic_blank_profile, "정우람", "우어어어어"));
		comments.add(new Comment("drawable://" + R.drawable.ic_blank_profile, "박준희", "추천 +1"));
		
		adapter.notifyDataSetChanged();
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
//		case R.id.action_edit:
//			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
