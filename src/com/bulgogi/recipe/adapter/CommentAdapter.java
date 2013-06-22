package com.bulgogi.recipe.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.model.Comment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class CommentAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Comment> comments;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	
	static class ViewHolder {
		ImageView ivProfile;
		TextView tvName;
		TextView tvComment;
	}
	
	public CommentAdapter(Context context, ArrayList<Comment> comments) {
		this.context = context;
		this.comments = comments;
		this.options = new DisplayImageOptions.Builder()
		.cacheInMemory()
		.cacheOnDisc()
		.resetViewBeforeLoading()
		.build();		
	}
	
	@Override
	public int getCount() {
		return comments.size();
	}

	@Override
	public Object getItem(int position) {
		return comments.get(position);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(context);
			convertView = inflator.inflate(R.layout.rl_comment, null);
			
			holder = new ViewHolder();
			holder.ivProfile = (ImageView)convertView.findViewById(R.id.iv_profile);
			holder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
			holder.tvComment = (TextView)convertView.findViewById(R.id.tv_comment);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		Comment comment = comments.get(position);
		imageLoader.displayImage(comment.getProfileUri(), holder.ivProfile, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});
		
		holder.tvName.setText(comment.getName());
		holder.tvComment.setText(comment.getComment());
		
		return convertView;
	}

}
