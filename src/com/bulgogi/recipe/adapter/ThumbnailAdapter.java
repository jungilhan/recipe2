package com.bulgogi.recipe.adapter;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.activity.RecipeActivity;
import com.bulgogi.recipe.application.RecipeApplication;
import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.model.Thumbnail;
import com.bulgogi.recipe.utils.TopRoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ThumbnailAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflator;
	private ArrayList<Thumbnail> thumbnails;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsThumbnail;
	private Rect imageBounds = new Rect();
	
	static class ViewHolder {
		View container;
		ImageView ivThumbnail;
		TextView tvEpisode;
		TextView tvChef;
		TextView tvFood;
		TextView tvLikeCount;
		TextView tvCommentCount;
		View pbLoading;		
	}
	
	public ThumbnailAdapter(Context context, ArrayList<Thumbnail> thumbnails) {
		this.context = context;
		this.inflator = LayoutInflater.from(context);
		this.thumbnails = thumbnails;
		this.optionsThumbnail = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.resetViewBeforeLoading(true)
			.bitmapConfig(Config.RGB_565)
			.displayer(new TopRoundedBitmapDisplayer((int) context.getResources().getDimension(R.dimen.thumbnail_round)))
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.build();
				
		int columns = RecipeApplication.isTablet() == true ? Constants.GRIDVIEW_TABLET_COLUMNS : Constants.GRIDVIEW_DEFAULT_COLUMNS;
		int imageWidth = (context.getResources().getDisplayMetrics().widthPixels / columns) 
				- context.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin)
				- context.getResources().getDimensionPixelSize(R.dimen.sgv_item_padding);
		int imageHeight = imageWidth;
		imageBounds.left = 0;
		imageBounds.top = 0;
		imageBounds.right = imageWidth;
		imageBounds.bottom = imageHeight;
	}

	@Override
	public int getCount() {
		return thumbnails.size();
	}

	@Override
	public Object getItem(int position) {
		return thumbnails.get(position);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		Thumbnail thumbnail = thumbnails.get(position);

		if (convertView == null) {
			convertView = inflator.inflate(R.layout.ll_thumbnail, null);

			holder = new ViewHolder();
			holder.container = convertView.findViewById(R.id.container);
			holder.pbLoading = convertView.findViewById(R.id.pb_loading);
			holder.ivThumbnail = (ImageView) convertView.findViewById(R.id.iv_thumbnail);
			holder.tvEpisode = (TextView) convertView.findViewById(R.id.tv_episode);
			holder.tvChef = (TextView) convertView.findViewById(R.id.tv_chef);
			holder.tvFood = (TextView) convertView.findViewById(R.id.tv_food);
			holder.tvLikeCount = (TextView) convertView.findViewById(R.id.tv_count_like);
			holder.tvCommentCount = (TextView) convertView.findViewById(R.id.tv_count_comment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, RecipeActivity.class);
				Thumbnail thumbnail = thumbnails.get(position);
				intent.putExtra(Extra.POST, (Serializable) thumbnail.getPost());
				context.startActivity(intent);
			}
		});

		holder.container.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Drawable drawable = holder.ivThumbnail.getDrawable();
				if (drawable == null) {
					return false;
				}

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					drawable.setColorFilter(0x22000000, PorterDuff.Mode.SRC_ATOP);
					holder.ivThumbnail.invalidate();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					drawable.clearColorFilter();
					holder.ivThumbnail.invalidate();
					break;
				}
				return false;
			}
		});

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(holder.ivThumbnail.getLayoutParams());
		lp.width = imageBounds.right - imageBounds.left;
		lp.width -= position == 0 ? context.getResources().getDimensionPixelSize(R.dimen.sgv_item_padding) : 0;
		lp.height = imageBounds.bottom - imageBounds.top;
		holder.ivThumbnail.setLayoutParams(lp);

		imageLoader.displayImage(thumbnail.getUrl(), holder.ivThumbnail, optionsThumbnail, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				holder.pbLoading.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				holder.pbLoading.setVisibility(View.GONE);

				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});

		holder.tvEpisode.setText(thumbnail.getEpisode() + context.getResources().getString(R.string.episode_postfix));
		holder.tvFood.setText(thumbnail.getFood());
		holder.tvChef.setText(thumbnail.getChef());
		holder.tvLikeCount.setText(Long.toString(thumbnail.getLikeCount()));
		holder.tvCommentCount.setText(Long.toString(thumbnail.getCommentCount()));

		return convertView;
	}
}
