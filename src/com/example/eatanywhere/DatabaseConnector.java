package com.example.eatanywhere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DatabaseConnector {
	private static final String DATABASE_NAME = "Content";
	SQLiteDatabase database;
	private DatabaseOpenHelper databaseOpenHelper;
	
	public DatabaseConnector(Context context) {
		databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
	}
	
	public void open() throws SQLException {
		database = databaseOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		if (database != null) 
			database.close();
	}
	
	public void insertContent(String content) {
		ContentValues newContent = new ContentValues();
		newContent.put("content", content);
		open();
		database.insert("contents", null, newContent);
		close();
	}
	
	public void rawQuery(String query) {
		Log.i("try to QUERY", query);
		database.execSQL(query);
	}
	public Cursor getAllContents() {
		return database.query("contents", new String[] {"_id", "content"}, null, null, null, null, "content");
	}
	
	public void deleteAll() {
		open();
		database.delete("contents", null, null);
		close();
	}
	
	private class DatabaseOpenHelper extends SQLiteOpenHelper {
		public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			//TODO warning note!
			//move the statement for onOpen here before release
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			String createQuery = "CREATE TABLE if not exists contents" +
			"(_id integer primary key autoincrement, " +
			"content TEXT);";
			db.execSQL(createQuery);
					
			String createFoodItem = "create table if not exists foodItem ("+
				    "creatime timestamp default current_timestamp,"+
				    "picName text,"+
				    "userId text,"+
				    "tag text,"+
				    "place text"+
				");";
			db.execSQL(createFoodItem);

			String createComment = "create table if not exists foodComment ("+
					"replyId int,"+
					"itemId int,"+
					"creatime timestamp default current_timestamp,"+
					"content text"+
					");";
			db.execSQL(createComment);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			
		}
	}

}
