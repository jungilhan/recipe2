package com.bulgogi.recipe.http.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Blog implements Serializable {
	private static final long serialVersionUID = 8936084090281747144L;
	
	public String query;
	public String total;
	public String start;
	public String display;
	public ArrayList<Item> items = new ArrayList<Item>();
	
	@Override
	public String toString() {
		return "{ total: " + total + "}";
	}
	
	public static class Item implements Serializable {
		private static final long serialVersionUID = 2779761830219146902L;
		
		public String title;
		public String link;
		public String description;
		public String bloggername;
		
		@Override
		public String toString() {
			return "{ title: " + title + ", link: " + link + ", description: " + description + ", bloggername: " + bloggername + "}";
		}
	}
}