package com.bulgogi.recipe.http.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Posts {
	/*
	 * The JSON result is here.
	 * https://public-api.wordpress.com/rest/v1/sites/52321301/posts/?&pretty=1
	 */
	
	@JsonProperty("status")
	public String status;
	
	@JsonProperty("count")
	public int count;
	
	@JsonProperty("count_total")
	public int countTotal;
	
	@JsonProperty("posts")
	public ArrayList<Post> posts;
	
	@Override
	public String toString() {
		return "{ status: " + status + " count: " + count + " countTotal: " + countTotal + " posts: " + posts.toString() + " }";
	}
}
