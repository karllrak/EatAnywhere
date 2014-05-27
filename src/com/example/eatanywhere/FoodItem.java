package com.example.eatanywhere;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class FoodItem implements Serializable, Comparable {
/*
  create table if not exists foodItem (
    creatime timestamp default current_timestamp,
    picName text,
    userId text,
    tag text,
    place text
);

 */
	//public static final int PICNAMEINDEX = 1;
	private int id = 0;
	private String picName=null;
	private String userId=null;
	private String tag=null;
	private String place=null;
	private boolean isLocal=false;
	private String creatime;
	private int score = 0;
	private String comment;
	
	public static boolean sortByTime = false;   
    public static boolean sortByScore = false;
	
	public void setCreatime( String t ) {
		creatime = t;
	}
	public String getCreatime() {
		return creatime;
	}
	public void setLocal() {
		isLocal = true;
	}
	public void setNonLocal() {
		isLocal = false;
	}
	public int getId() { 
		return id;
	}
	public void setId( int id ) {
		this.id = id;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPicName() {
		return picName;
	}
	public void setPicName(String picName) {
		this.picName = picName;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int compareTo(Object ano) {
		if (sortByTime) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date0 = null, date1 = null;
			try {
				date0 = sdf.parse(this.creatime);
				date1 = sdf.parse(((FoodItem)ano).creatime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			long s0 = date0.getTime();
			long s1 = date1.getTime();
			if (s0 > s1) return -1;
			else if (s0 < s1) return 1;
			else return 0;
		} else if (sortByScore) {
			return ((FoodItem)ano).score - this.score;
		}
		return 0;
	}
}
