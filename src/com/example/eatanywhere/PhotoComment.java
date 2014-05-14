package com.example.eatanywhere;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoComment extends Activity {

	private String picName = null;
	private String mComment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		picName = getIntent().getStringExtra("picName");
		setContentView(R.layout.photocomment);
		loadPreviewImage();
	}

	private void loadPreviewImage() {
		ImageView imgView = (ImageView)findViewById(R.id.photoCommentImgView);
		ListViewImageActivity.loadImageFromPath(imgView, picName);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void saveComment( View v ) {
		mComment = ((EditText)findViewById(R.id.comment)).getText().toString();
		if ( mComment.matches("") ) {
			Toast.makeText(this, "请输入评论", Toast.LENGTH_SHORT).show();
			return;
		}

		AsyncTask<Object, Object, Object> saveFoodItemTask = 
				new AsyncTask<Object, Object, Object>() {
				DatabaseConnector databaseConnector = new DatabaseConnector(PhotoComment.this);
			@Override
			protected Object doInBackground(Object... params) {
				String query = "insert into foodItem( userId, picName ) values ("+
						" 'isb911', "+ " '"+picName+"' );";
				databaseConnector.open();
				databaseConnector.rawQuery(query);
				String rowidQuery = "select count(*) from foodItem";
				Cursor c = databaseConnector.database.rawQuery(rowidQuery, null);
				c.moveToFirst();
				int rowid = c.getInt(0);
				c.close();
				ContentValues content = new ContentValues();
				content.put("itemId", rowid);
				content.put("content", mComment);
				databaseConnector.database.insert("foodComment", null, content);
				return null;
			} 

			@Override
			protected void onPostExecute(Object result) {
				databaseConnector.close();
				finish(); 
			} 
		}; 
        saveFoodItemTask.execute((Object[]) null); 
	}

	public void cancelComment( View v ) {
		this.finish();
	}

}
