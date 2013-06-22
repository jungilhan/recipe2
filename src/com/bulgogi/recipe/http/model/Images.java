package com.bulgogi.recipe.http.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Images implements Serializable {
	private static final long serialVersionUID = 6211721822173397065L;

	@JsonProperty("full")
	public Image full;
	
	@JsonProperty("thumbnail")
	public Image thumbnail;
	
	@JsonProperty("medium")
	public Image medium;
	
	@JsonProperty("large")
	public Image large;
}
