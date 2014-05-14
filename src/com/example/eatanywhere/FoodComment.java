package com.example.eatanywhere;

public class FoodComment {
	/*
	create table if not exists foodComment (
		    replyId int,
		    itemId int,
		    creatime timestamp default current_timestamp,
		    content text
		);
	*/
	private int replyId;
	private int itemId;
	private String content;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getReplyId() {
		return replyId;
	}
	public void setReplyId(int replyId) {
		this.replyId = replyId;
	}


}
