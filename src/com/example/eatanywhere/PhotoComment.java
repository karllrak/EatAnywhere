package com.example.eatanywhere;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

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
	

}
