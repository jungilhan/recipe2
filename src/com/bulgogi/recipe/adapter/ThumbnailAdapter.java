package com.bulgogi.recipe.adapter;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.widget.StaggeredGridView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.activity.RecipeActivity;
import com.bulgogi.recipe.config.Constants.Extra;
import com.bulgogi.recipe.model.Thumbnail;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class ThumbnailAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflator;
	private ArrayList<Thumbnail> thumbnails;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private Rect imageBounds = new Rect();
	
	static class ViewHolder {
		TextView tvChef;
		ImageView ivThumbnail;
	}
	
	public ThumbnailAdapter(Context context, ArrayList<Thumbnail> thumbnails) {
		this.context = context;
		this.inflator = LayoutInflater.from(context);
		this.thumbnails = thumbnails;
		this.options = new DisplayImageOptions.Builder()
		.cacheInMemory()
		.cacheOnDisc()
		.resetViewBeforeLoading()
		.build();
		
		int imageWidth = (context.getResources().getDisplayMetrics().widthPixels / 2) 
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
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		switch (position) {
		case 0:
			convertView = inflator.inflate(R.layout.ll_thumbnail_large, parent, false);
			StaggeredGridView.LayoutParams lp = new StaggeredGridView.LayoutParams(convertView.getLayoutParams());
			lp.span = 2;
			convertView.setLayoutParams(lp);
			break;
		default:
			convertView = inflator.inflate(R.layout.ll_thumbnail, parent, false);
			lp = new StaggeredGridView.LayoutParams(convertView.getLayoutParams());
			lp.span = 1;
			convertView.setLayoutParams(lp);
			break;
		}
		
		Thumbnail thumbnail = thumbnails.get(position);
		
		View container = convertView.findViewById(R.id.container);
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, RecipeActivity.class);
		        Thumbnail thumbnail = thumbnails.get(position);
		        intent.putExtra(Extra.POST, (Serializable)thumbnail.getPost());
		        context.startActivity(intent);
			}
		});
		
		TextView tvEpisode = (TextView)convertView.findViewById(R.id.tv_episode);
		tvEpisode.setText(thumbnail.getEpisode());

		TextView tvEpisodePostfix = (TextView)convertView.findViewById(R.id.tv_episode_postfix);
		tvEpisodePostfix.setText(context.getResources().getString(R.string.episode_postfix));
		
		ImageView ivThumbnail = (ImageView)convertView.findViewById(R.id.iv_thumbnail);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ivThumbnail.getLayoutParams());
		lp.width = imageBounds.right - imageBounds.left;
        lp.width -= position == 0 ? context.getResources().getDimensionPixelSize(R.dimen.sgv_item_padding) : 0;
        lp.height = imageBounds.bottom - imageBounds.top;
        ivThumbnail.setLayoutParams(lp);

		imageLoader.displayImage(thumbnail.getUrl(), ivThumbnail, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});
		
		ImageView ivChef = (ImageView)convertView.findViewById(R.id.iv_chef);
		String uri = thumbnail.getChefImageUrl() != null ? thumbnail.getChefImageUrl() : "drawable://" + R.drawable.ic_blank_profile;
		imageLoader.displayImage(uri, ivChef, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});
		
		TextView tvFood = (TextView)convertView.findViewById(R.id.tv_food);
		tvFood.setText(thumbnail.getFood());
		
		TextView tvChef = (TextView)convertView.findViewById(R.id.tv_chef);
		tvChef.setText(thumbnail.getChef());
		
		return convertView;
	}	
}
