package com.bulgogi.recipe.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.http.model.Like;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class LikeUserAdapter  extends BaseAdapter {
	private Context context;
	private ArrayList<Like> likeUsers;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	static class ViewHolder {
		ImageView ivProfile;
		TextView tvName;
	}

	public LikeUserAdapter(Context context, ArrayList<Like> likePeople) {
		this.context = context;
		this.likeUsers = likePeople;
		this.options = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
			.resetViewBeforeLoading()
			.showImageForEmptyUri(R.drawable.ic_blank_profile)
			.showImageOnFail(R.drawable.ic_blank_profile)
			.showStubImage(R.drawable.ic_blank_profile)
			.bitmapConfig(Config.RGB_565)
			.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelSize(R.dimen.profile_round)))
			.build();
	}

	@Override
	public int getCount() {
		return likeUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return likeUsers.get(position);
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
			convertView = inflator.inflate(R.layout.ll_like_user_item, null);

			holder = new ViewHolder();
			holder.ivProfile = (ImageView) convertView.findViewById(R.id.iv_profile);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Like likeUser = likeUsers.get(position);
		if (likeUser.name == null) {
			holder.ivProfile.setVisibility(View.INVISIBLE);
		} else {
			holder.tvName.setText(likeUser.name);
			holder.ivProfile.setVisibility(View.VISIBLE);			
			String url = "http://graph.facebook.com/" + likeUser.facebookId + "/picture?type=normal";
			imageLoader.displayImage(url, holder.ivProfile, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
					view.setAnimation(animation);
					animation.start();
				}
			});
		}
		
		return convertView;
	}

}
