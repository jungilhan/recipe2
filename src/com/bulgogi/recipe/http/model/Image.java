package com.bulgogi.recipe.http.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Image implements Serializable {
	private static final long serialVersionUID = 7032023669182845722L;

	@JsonProperty("url")
	public String url;

	@JsonProperty("width")
	public int width;
	
	@JsonProperty("height")
	public int height;
}
