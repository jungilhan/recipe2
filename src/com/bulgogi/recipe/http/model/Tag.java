package com.bulgogi.recipe.http.model;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tag implements Serializable {
	private static final long serialVersionUID = -5835483524629580232L;
	
	@JsonProperty("id")
	public int id;
	
	@JsonProperty("title")
	public String title;
	
	public String episode() {
		String[] parts = Pattern.compile("|", Pattern.LITERAL).split(title); 
		return parts[0];
	}
	
	public String date() {
		String[] parts = Pattern.compile("|", Pattern.LITERAL).split(title); 
		return parts[1];
	}
	
	public String chef() {
		String[] parts = Pattern.compile("|", Pattern.LITERAL).split(title); 
		return parts[2];
	}
	
	public String food() {
		String[] parts = Pattern.compile("|", Pattern.LITERAL).split(title); 
		return parts[3];
	}
	
	public String youtubeId() {
		String[] parts = Pattern.compile("|", Pattern.LITERAL).split(title); 
		return parts[4];
	}
}
