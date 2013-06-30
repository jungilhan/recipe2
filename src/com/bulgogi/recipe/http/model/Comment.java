package com.bulgogi.recipe.http.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Comment {
	@JsonProperty("post_id")
	public int postId;
	
	@JsonProperty("id")
	public int commentId;
	
	@JsonProperty("user_name")
	public String name;
	
	@JsonProperty("fb_id")
	public String facebookId;
	
	@JsonProperty("thumb_url")
	public String thumbnail;

	@JsonProperty("timestamp")
	public String timestamp;

	@JsonProperty("comment")
	public String comment;
	
	@Override
	public String toString() {
		return "{ post_id: " + postId + " id: " + commentId + " user_name: " + name + " fb_id: " + facebookId 
				+ " thumb_url: " + thumbnail + " timestamp: " + timestamp + " comment: " + comment + " }";
	}
}
