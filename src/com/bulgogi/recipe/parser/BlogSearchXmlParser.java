package com.bulgogi.recipe.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.bulgogi.recipe.config.Constants;
import com.bulgogi.recipe.http.model.Blog;
import com.bulgogi.recipe.http.model.Blog.Item;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class BlogSearchXmlParser {
	private static final String TAG = BlogSearchXmlParser.class.getSimpleName();
	private static final String TAG_TOTAL = "total";
	private static final String TAG_DISPLAY = "display";
	private static final String TAG_START = "start";
	private static final String TAG_ITEM = "item";
	private static final String TAG_TITLE = "title";
	private static final String TAG_LINK = "link";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_BLOGGERNAME = "bloggername";
	
	private OnParserListener onParserListener;

	public void setOnParserListener(OnParserListener onParserListener) {
		this.onParserListener = onParserListener;
	}
	
	public interface OnParserListener {
		public void onComplete(Blog blog);
	}
	
	public void read(String url) throws XmlPullParserException, IOException {
		final Blog blog = new Blog();
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
				try {
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					XmlPullParser parser = factory.newPullParser();
					parser.setInput(new StringReader(response));
					
					final int depth = parser.getDepth();
			    	int type;
			    	while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
			    		if (type != XmlPullParser.START_TAG) {
							continue;
						}
			    		
			    		String name = parser.getName();
			    		if (name.equals(TAG_TOTAL)) {
			    			blog.total = readText(parser);
			    		} else if (name.equals(TAG_DISPLAY)) {
			    			blog.display = readText(parser);
			    		} else if (name.equals(TAG_START)) {
			    			blog.start = readText(parser);
			    		} else if (name.equals(TAG_ITEM)) {
			    			Item item = new Item();
			    		
			    			int folderDepth = parser.getDepth();
			    			while ((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > folderDepth) {
			    				if (type != XmlPullParser.START_TAG) {
									continue;
								}
			    				
			    				name = parser.getName();
				    			if (name.equals(TAG_TITLE)) {
					    			item.title = readText(parser);
					    		} else if (name.equals(TAG_LINK)) {
					    			item.link = readText(parser);
					    		} else if (name.equals(TAG_DESCRIPTION)) {
					    			item.description = readText(parser);
					    		} else if (name.equals(TAG_BLOGGERNAME)) {
					    			item.bloggername = readText(parser);
					    		}
			    			}
			    			
			    			blog.items.add(item);
			    			if (Constants.Config.DEBUG) {
			    				Log.d(TAG, "item: " + item.toString());
			    			}
			    		} 
			    	}
			    	
			    	if (onParserListener != null) {
			    		onParserListener.onComplete(blog);
			    	}
					
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
		    }
		});
	}
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	} 
}
