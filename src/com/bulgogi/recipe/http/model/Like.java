package com.bulgogi.recipe.http.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Like implements Serializable {
	private static final long serialVersionUID = -8437715068539058267L;

	@JsonProperty("id")
	public int likeId;
	
	@JsonProperty("post_id")
	public int postId;
	
	@JsonProperty("fb_id")
	public long facebookId;
		
	public String name;
	
	@Override
	public String toString() {
		return "{ post_id: " + postId + " id: " + likeId + " fb_id: " + facebookId + " }";
	}
}