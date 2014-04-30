package com.example.eatanywhere;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoComment extends Activity {

	private String picName = null;
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
		String comment = ((EditText)findViewById(R.id.comment)).getText().toString();
		if ( comment.matches("") ) {
			Toast.makeText(this, "请输入评论", Toast.LENGTH_SHORT).show();
			return;
		}

		AsyncTask<Object, Object, Object> saveFoodItemTask = 
				new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				DatabaseConnector databaseConnector = new DatabaseConnector(PhotoComment.this);
				String query = "insert into foodItem( userId, picName ) values ("+
						" 'isb911', "+ " '"+picName+"' );";
				databaseConnector.rawQuery(query);
				return null;
			} 

			@Override
			protected void onPostExecute(Object result) {
				finish(); 
			} 
		}; 
        saveFoodItemTask.execute((Object[]) null); 
	}

	public void cancelComment( View v ) {
		this.finish();
	}

}
