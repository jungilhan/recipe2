package com.bulgogi.recipe.http.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Count {	
	@JsonProperty("post_id")
	public int postId;
	
	@JsonProperty("count")
	public long count;

	@JsonProperty("like")
	public long like;
	
	@JsonProperty("comment")
	public long comment;
	
	@Override
	public String toString() {
		return "{ post_id: " + postId + " like: " + like + " comment: " + comment + " }";
	}
}