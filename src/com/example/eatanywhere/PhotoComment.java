package com.example.eatanywhere;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class PhotoComment extends Activity {

	private String mPicName = null;
	private String mPlace = null;
	private String mComment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPicName = getIntent().getStringExtra("picName");
		setContentView(R.layout.photocomment);
		Spinner placeSelectSpinner = (Spinner) findViewById(R.id.photocomment_spinner);
		String[] placeList = PhotoComment.getPlaceList();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, placeList);
		placeSelectSpinner.setAdapter(adapter);
		loadPreviewImage();
	}

	private static String[] getPlaceList() {
		// TODO Auto-generated method stub
		return new String[]{ "一食堂", "二食堂", "三食堂" };
	}

	private void loadPreviewImage() {
		ImageView imgView = (ImageView)findViewById(R.id.photoCommentImgView);
		ListViewImageActivity.loadImageFromPath(imgView, mPicName);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void saveComment( View v ) {
		mComment = ((EditText)findViewById(R.id.comment)).getText().toString();
		mPlace = (String) ((Spinner)findViewById(R.id.photocomment_spinner)).getSelectedItem();
		if ( mComment.matches("") ) {
			Toast.makeText(this, "请输入评论", Toast.LENGTH_SHORT).show();
			return;
		}

		AsyncTask<Object, Object, Object> saveFoodItemTask = 
				new AsyncTask<Object, Object, Object>() {
				DatabaseConnector databaseConnector = new DatabaseConnector(PhotoComment.this);
			@Override
			protected Object doInBackground(Object... params) {
				//get the selected place
				String query = "insert into foodItem( userId, picName, place ) values ("+
						" 'isb911', " +
						" '"+mPicName+"', "+
						" '"+mPicName+"' "+
						" );";
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
