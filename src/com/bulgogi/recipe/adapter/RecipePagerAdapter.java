package com.bulgogi.recipe.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.http.model.Image;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class RecipePagerAdapter extends PagerAdapter {
	private Context context; 
	private LayoutInflater inflater;
	private ArrayList<Image> images = new ArrayList<Image>();
	private DisplayImageOptions options;
	
	public RecipePagerAdapter(Context context, ArrayList<Image> images) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.images = images;
		this.options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.resetViewBeforeLoading(true)
		.build();
	}
	
	@Override
	public Object instantiateItem(View pager, int position) {
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.ll_recipe_pager_item, null);
		ImageView ivRecipe = (ImageView) layout.findViewById(R.id.iv_recipe);
		final ProgressBar pbLoading = (ProgressBar) layout.findViewById(R.id.pb_loading);

		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(images.get(position).url, ivRecipe, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				pbLoading.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				pbLoading.setVisibility(View.GONE);

				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});

		((ViewPager) pager).addView(layout, 0);
		return layout;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);

		ViewPager pager = (ViewPager) container;

		int srcWidth = images.get(position).width;
		int srcHeight = images.get(position).height;

		if (srcWidth > srcHeight) {
			int displayWidth = pager.getResources().getDisplayMetrics().widthPixels;
			float scale = (float) displayWidth / srcWidth;
			float scaledHeight = srcHeight * scale;
			pager.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, Math.max((int) scaledHeight, 1)));
		}
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager) pager).removeView((View) view);
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public boolean isViewFromObject(View pager, Object obj) {
		return pager == obj;
	}
}
