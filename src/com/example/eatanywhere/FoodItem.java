package com.example.eatanywhere;

public class FoodItem {
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
}
