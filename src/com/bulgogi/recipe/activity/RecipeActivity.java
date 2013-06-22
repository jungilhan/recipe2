package com.bulgogi.recipe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bulgogi.recipe.R;
import com.bulgogi.recipe.adapter.RecipePagerAdapter;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.http.model.Post;
import com.viewpagerindicator.CirclePageIndicator;

public class RecipeActivity extends SherlockActivity {
	
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
		
		final ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new RecipePagerAdapter(this, post.getRecipes()));

		CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);		
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
		case R.id.action_edit:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
