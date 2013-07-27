package com.bulgogi.recipe.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
import com.bulgogi.recipe.activity.RecipeActivity;
import com.bulgogi.recipe.http.model.Comment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

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
		TextView tvEmpty;
	}

	public CommentAdapter(Context context, ArrayList<Comment> comments) {
		this.context = context;
		this.comments = comments;
		this.options = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.resetViewBeforeLoading(true)
			.showImageForEmptyUri(R.drawable.ic_blank_profile)
			.showImageOnFail(R.drawable.ic_blank_profile)
			.showStubImage(R.drawable.ic_blank_profile)
			.bitmapConfig(Config.RGB_565)
			.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelSize(R.dimen.profile_round)))
			.build();
	}

	@Override
	public int getCount() {
		// [XXX] PullToRefresh에서 아이템이 하나도 없을 때 스크롤 시 상단 잘리는 현상 우회처리
		if (comments.size() == 0 && context instanceof RecipeActivity && !((RecipeActivity)context).isLoading()) {
			return 1;
		}
		
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
			holder.ivProfile = (ImageView) convertView.findViewById(R.id.iv_profile);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tvComment = (TextView) convertView.findViewById(R.id.tv_comment);
			holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tv_timestamp);
			holder.tvEmpty = (TextView) convertView.findViewById(R.id.tv_empty_comments);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (comments.size() == 0 && position == 0) {
			holder.ivProfile.setVisibility(View.INVISIBLE);
			holder.tvName.setVisibility(View.INVISIBLE);
			holder.tvComment.setVisibility(View.INVISIBLE);
			holder.tvTimestamp.setVisibility(View.INVISIBLE);
			holder.tvEmpty.setVisibility(View.VISIBLE);
			return convertView;
		} else {
			holder.ivProfile.setVisibility(View.VISIBLE);
			holder.tvName.setVisibility(View.VISIBLE);
			holder.tvComment.setVisibility(View.VISIBLE);
			holder.tvTimestamp.setVisibility(View.VISIBLE);
			holder.tvEmpty.setVisibility(View.GONE);
		}
		
		Comment comment = comments.get(position);
		String url = comment.thumbnail;
		if (url.contains("?type=small")) {
			url = url.replace("?type=small", "?type=normal");
		}
		imageLoader.displayImage(url, holder.ivProfile, options, new SimpleImageLoadingListener() {
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
