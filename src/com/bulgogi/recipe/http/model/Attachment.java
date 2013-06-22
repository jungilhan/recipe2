package com.bulgogi.recipe.http.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Attachment implements Serializable {
	private static final long serialVersionUID = 4471499741805682269L;

	@JsonProperty("url")
	public String url;

	@JsonProperty("images")
	public Images images;
	
	
}
