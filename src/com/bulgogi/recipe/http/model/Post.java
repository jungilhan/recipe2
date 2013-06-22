package com.bulgogi.recipe.http.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Post implements Serializable {
	private static final long serialVersionUID = 1235732155433879598L;
	private static final String CHEF_PREFIX = "creator_";
	private static final String RECIPE_PREFIX = "recipe";
	private static final String THUMBNAIL_PREFIX = "thumbnail";

	@JsonProperty("id")
	public int id;
	
	@JsonProperty("date")
	public String date;
	
	@JsonProperty("title")
	public String title;
	
	@JsonProperty("content")
	public String content;

	@JsonProperty("tags")
	public ArrayList<Tag> tags;
	
	@JsonProperty("attachments")
	public ArrayList<Attachment> attachments;
	
	public Image getChef() {
		for (Attachment attachment : attachments) {
			if (attachment.url.contains(CHEF_PREFIX)) {
				return attachment.images.full;
			}
		}
		return null;
	}
	
	public ArrayList<Image> getRecipes() {
		ArrayList<Image> recipes = new ArrayList<Image>();
		for (Attachment attachment : attachments) {
			if (attachment.url.contains(RECIPE_PREFIX)) {
				recipes.add(attachment.images.full);
			}
		}
		
		Collections.sort(recipes, new Comparator<Image>(){
			public int compare(Image s1, Image s2) {
				return s1.url.compareTo(s2.url);
			}
		});
		
		return recipes;
	}
	
	public Image getThumbnail() {
		for (Attachment attachment : attachments) {
			if (attachment.url.contains(THUMBNAIL_PREFIX)) {
				return attachment.images.full;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "{ id: " + id + " date: " + date + " title: " + title + " content: " + content + " }";
	}
};