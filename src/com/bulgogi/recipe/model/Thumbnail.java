package com.bulgogi.recipe.model;

import com.bulgogi.recipe.R;
import com.bulgogi.recipe.http.model.Post;

public class Thumbnail {	
	private Post post;
	
	public Thumbnail(Post post) {
		this.post = post;
	}
	
	public int getId() {
		return post.id;
	}
	
	public Post getPost() {
		return post;
	}
	
	public String getUrl() {
		return post.getThumbnail().url;
	}

	public String getEpisode() {
		return post.tags.get(0).episode();
	}
	
	public String getFood() {
		return post.tags.get(0).food();
	}
	
	public String getDate() {
		return post.tags.get(0).date();
	}
	
	public String getChef() {
		return post.tags.get(0).chef();
	}
	
	public String getChefImageUri() {
		if (post.getChef() == null) {
			return "drawable://" + R.drawable.ic_blank_profile;
		}
		
		return post.getChef().url;
	}
}
