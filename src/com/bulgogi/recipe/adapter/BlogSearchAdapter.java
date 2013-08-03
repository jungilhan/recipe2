package com.bulgogi.recipe.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.http.model.Blog.Item;

public class BlogSearchAdapter  extends BaseAdapter {
	private ArrayList<Item> items;
	private LayoutInflater inflator;

	static class ViewHolder {
		TextView tvTitle;
		TextView tvBlog;
		TextView tvDescription;
	}

	public BlogSearchAdapter(Context context, ArrayList<Item> items) {
		this.items = items;
		inflator = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflator.inflate(R.layout.ll_blog_search_item, null);

			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tvBlog = (TextView) convertView.findViewById(R.id.tv_blog);
			holder.tvDescription = (TextView) convertView.findViewById(R.id.tv_description);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Item item = items.get(position);
		holder.tvTitle.setText(Html.fromHtml(item.title));
		holder.tvBlog.setText(item.bloggername);
		String description = item.description.replaceAll("\\<.*?\\>", "");
		description = description.replaceAll("&.*;", "");
		holder.tvDescription.setText(description );
		
		return convertView;
	}
}