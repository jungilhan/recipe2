package com.bulgogi.recipe.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.http.model.Comment;
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
		TextView tvTimestamp;
	}
	
	public CommentAdapter(Context context, ArrayList<Comment> comments) {
		this.context = context;
		this.comments = comments;
		this.options = new DisplayImageOptions.Builder()
		.cacheInMemory()
		.cacheOnDisc()
		.resetViewBeforeLoading()
		.showImageForEmptyUri(R.drawable.ic_blank_profile)
		.showImageOnFail(R.drawable.ic_blank_profile)
		.showStubImage(R.drawable.ic_blank_profile)
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
			holder.tvTimestamp = (TextView)convertView.findViewById(R.id.tv_timestamp);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		Comment comment = comments.get(position);
		imageLoader.displayImage(comment.thumbnail, holder.ivProfile, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
				view.setAnimation(animation);
				animation.start();
			}
		});
		
		holder.tvName.setText(comment.name);
		holder.tvComment.setText(comment.comment);
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formatter.parse(comment.timestamp.substring(0, 24)));
			calendar.add(Calendar.HOUR, 9);
			String timestamp = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis()).toString();
			holder.tvTimestamp.setText(timestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return convertView;
	}

}
