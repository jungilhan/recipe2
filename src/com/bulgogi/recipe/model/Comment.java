package com.bulgogi.recipe.model;

public class Comment {
	private String profileUri;
	private String name;
	private String comment;
	
	public Comment(String profileUri, String name, String comment) {
		this.profileUri = profileUri;
		this.name = name;
		this.comment = comment;
	}
	
	public String getProfileUri() {
		return profileUri;
	}
	
	public String getName() {
		return name;
	}
	
	public String getComment() {
		return comment;
	}
}
