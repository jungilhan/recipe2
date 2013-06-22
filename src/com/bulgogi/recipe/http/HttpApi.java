package com.bulgogi.recipe.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.squareup.okhttp.OkHttpClient;

public class HttpApi {
	private OkHttpClient client;	
	
	public HttpApi() {
		client = new OkHttpClient();
	}
	
	public String get(String url) throws IOException {
		HttpURLConnection connection = client.open(new URL(url));
		InputStream in = null;
		try {
			in = connection.getInputStream();
			byte[] response = readFully(in);
			return new String(response, "UTF-8");
		} finally {
			if (in != null) in.close();
		}
	}
	
	byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int count; (count = in.read(buffer)) != -1; ) {
			out.write(buffer, 0, count);
		}
		
		return out.toByteArray();
	}
}
