package com.bulgogi.recipe.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

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
			if (in != null) {
				in.close();
			}
		}
	}

	byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int count; (count = in.read(buffer)) != -1;) {
			out.write(buffer, 0, count);
		}

		return out.toByteArray();
	}
	
	public void post(String url, List<NameValuePair> params) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			post.setEntity(ent);
			
			HttpResponse responsePOST = client.execute(post);
			HttpEntity resEntity = responsePOST.getEntity();

			if (resEntity != null) {
				Log.i("RESPONSE", EntityUtils.toString(resEntity));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
