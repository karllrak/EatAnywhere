package com.example.eatanywhere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseConnector {
	private static final String DATABASE_NAME = "Content";
	private SQLiteDatabase database;
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
			String createQuery = "CREATE TABLE contents" +
			"(_id integer primary key autoincrement, " +
			"content TEXT);";
			db.execSQL(createQuery);
					
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			
		}
	}

}
