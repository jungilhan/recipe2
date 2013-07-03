package com.bulgogi.recipe.http.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Like {
	@JsonProperty("id")
	public int likeId;
	
	@JsonProperty("post_id")
	public int postId;
	
	@JsonProperty("fb_id")
	public long facebookId;
		
	@Override
	public String toString() {
		return "{ post_id: " + postId + " id: " + likeId + " fb_id: " + facebookId + " }";
	}
}