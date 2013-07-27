package com.bulgogi.recipe.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.bulgogi.recipe.http.model.Count;
import com.bulgogi.recipe.model.Thumbnail;

public class Sort {
	public static void byNewest(ArrayList<Thumbnail> thumbnails) {
    	Collections.sort(thumbnails, new Comparator<Thumbnail>() {
			public int compare(Thumbnail s1, Thumbnail s2) {
				if (s1.getId() > s2.getId()) {
					return -1;
    			} else if (s1.getId() < s2.getId()) {
					return 1;
    			} else {
					return 0;
    			}
			}
		});
	}
	
	public static void byViewCount(ArrayList<Thumbnail> thumbnails, final HashMap<Integer, Count> countMap) {
		Collections.sort(thumbnails, new Comparator<Thumbnail>() {
			public int compare(Thumbnail s1, Thumbnail s2) {
				Count c1 = countMap.get(s1.getId());
				Count c2 = countMap.get(s2.getId());
				
				if (c1 != null && c2 == null) {
					return -1;
				} else if (c1 == null && c2 != null) {
					return 1;
				} else if (c1 == null && c2 == null) {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
				
				if (c1.count > c2.count) {
					return -1;
				} else if (c1.count < c2.count) {
					return 1;
				} else {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
	}
	
	public static void byLikeCount(ArrayList<Thumbnail> thumbnails, final HashMap<Integer, Count> countMap) {
		Collections.sort(thumbnails, new Comparator<Thumbnail>() {
			public int compare(Thumbnail s1, Thumbnail s2) {
				Count c1 = countMap.get(s1.getId());
				Count c2 = countMap.get(s2.getId());
				
				if (c1 != null && c2 == null) {
					return -1;
				} else if (c1 == null && c2 != null) {
					return 1;
				} else if (c1 == null && c2 == null) {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
				
				if (c1.likes > c2.likes) {
					return -1;
				} else if (c1.likes < c2.likes) {
					return 1;
				} else {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
	}
	
	public static void byCommentCount(ArrayList<Thumbnail> thumbnails, final HashMap<Integer, Count> countMap) {
		Collections.sort(thumbnails, new Comparator<Thumbnail>() {
			public int compare(Thumbnail s1, Thumbnail s2) {
				Count c1 = countMap.get(s1.getId());
				Count c2 = countMap.get(s2.getId());
				
				if (c1 != null && c2 == null) {
					return -1;
				} else if (c1 == null && c2 != null) {
					return 1;
				} else if (c1 == null && c2 == null) {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
				
				if (c1.comments > c2.comments) {
					return -1;
				} else if (c1.comments < c2.comments) {
					return 1;
				} else {
					if (s1.getId() > s2.getId()) {
						return -1;
					} else if (s1.getId() > s2.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
	}
}
