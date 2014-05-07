package com.example.eatanywhere;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

public class ListViewImageActivity extends Activity {
	//FIXME: why we need asynctask when getPicCommentList
	public static String picDirPath=Environment.getExternalStorageDirectory()+"/1pic/";
	private String picFileType="jpeg";
	private HashMap mItemList;
	private FoodItem[] mFoodItemList=null;
	private FoodComment[] mCommentList=null;
	private ScrollView mScrollView=null;
	private String placeToEat=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//显示从哪个食堂点击进来的
		Intent it = getIntent();
		placeToEat = it.getStringExtra("placeToEat");
		Toast.makeText(this, placeToEat, Toast.LENGTH_SHORT).show();

		mScrollView = new ScrollView(this);
		String[] picFullPathNameArray = getPicNamePathList();
		getPicCommentList();
		//filter the image by name( by the XXXXn.jpeg where n is the last number before dot
		//e.g. 6->0 7->1
		picFullPathNameArray = filterPictureByName(placeToEat, picFullPathNameArray);


		LinearLayout totalLayout = new LinearLayout(this);
		totalLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout topBtnLayout = new LinearLayout(this);
		Button btn = new Button(this);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent it = new Intent();
				it.setClass(ListViewImageActivity.this, CameraCapture.class);
				startActivity(it);
			}});
		btn.setText("拍照");
		Button btnReload = new Button(this);
		btnReload.setText("刷新");
		btnReload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				reload();
			}});
		topBtnLayout.addView(btn);
		topBtnLayout.addView(btnReload);
		LinearLayout imageLayout = new LinearLayout(this);
		imageLayout.setOrientation(LinearLayout.VERTICAL);
		loadImageList(imageLayout, picFullPathNameArray);

		totalLayout.addView(topBtnLayout);
		totalLayout.addView(imageLayout);
		mScrollView.addView(totalLayout);
		//setContentView(R.layout.activity_main);
		setContentView(mScrollView);
	}

	private void reload(){
		getPicCommentList();
		LinearLayout imageLayout = (LinearLayout) mScrollView.getChildAt(0);
		imageLayout = (LinearLayout) imageLayout.getChildAt(1);
		imageLayout.removeAllViews();
		String[] newPicNameList = getPicNamePathList();
		newPicNameList = filterPictureByName(placeToEat,newPicNameList);
		loadImageList(imageLayout,newPicNameList);
		/* so asynctask 
		newPicNameList = filterPictureByName(placeToEat,newPicNameList);
		loadImageList(imageLayout,newPicNameList);
		 */
	}

	private void getPicCommentList() {
		//load the picture comment into mFoodComment
		DatabaseConnector databaseConnector = new DatabaseConnector(ListViewImageActivity.this);
		databaseConnector.open();
		//load the picname first
		Cursor c = databaseConnector.database.query("foodItem", new String[] {"picName", "rowid"}, null, null, null, null, null);
		Log.e("", ""+c.getCount());
		mFoodItemList = new FoodItem[c.getCount()];
		for (int i = 0; i < mFoodItemList.length; i++ ) {
			mFoodItemList[i] = new FoodItem();
		}
		String tmpPicName = null;
		int i = 0;
		int rowid = 0;
		while ( c.moveToNext() ) {
			tmpPicName = c.getString(0);
			rowid = c.getInt(1);
			mFoodItemList[i].setPicName(tmpPicName);
			mFoodItemList[i].setId(rowid);

			i++;
		}
		c.close();
		//load the comments
		c = databaseConnector.database.query("foodComment", 
				new String[] {"itemId", "content"}, null, null, null, null, null);
		mCommentList = new FoodComment[c.getCount()]; 
		for (int j = 0; j < mCommentList.length; j++ ) {
			mCommentList[j] = new FoodComment();
		}
		i = 0;
		int tmpId = 0;
		String tmpContent = null;
		while ( c.moveToNext() ) {
			tmpId = c.getInt(0);
			tmpContent = c.getString(1);
			mCommentList[i].setContent(tmpContent);
			mCommentList[i].setItemId(tmpId);
			i++;
		}
		c.close();
		databaseConnector.close();
	}

	private String[] getPicNamePathList() {
		File picDir = new File(picDirPath);
		return picDir.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String filename) {
				if ( filename.endsWith(picFileType) ){
					return true;
				}
				return false;
			}});
	}

	private String[] filterPictureByName(String placeToEat, String[] arr ) {
		int n = 0;
		if ( placeToEat.equals("one") ) {
			n = 0;
		} else if ( placeToEat.equals("two") ) {
			n = 1;
		} else {
			n = 2;
		}
		Vector tmp = new Vector();
		for ( int i = 0; i < arr.length; i++ ) {
			int idx = arr[i].indexOf(".");
			int num = Integer.parseInt(arr[i].substring(idx-1,idx));
			if ( n == (num%3) ){
				tmp.add(arr[i]);
			}
		}
		String[] result = new String[tmp.size()];
		tmp.copyInto(result);
		return result;
	}

	public static boolean loadImageFromPath(ImageView imgView, String imgPath ) {
		if ( null == imgView ) {
			return false;
		}
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(picDirPath+imgPath);
			Bitmap bmp = BitmapFactory.decodeStream(fin);
			imgView.setImageBitmap(bmp);
			imgView.setAdjustViewBounds(true);
			imgView.setMaxHeight(432);
			imgView.setMaxWidth(264);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
	}

	private void rotateImage(ImageView imgView) {
		Matrix matrix=new Matrix();
		imgView.setScaleType(ScaleType.MATRIX);   //required
		matrix.postRotate((float) 90f,imgView.getDrawable()
				.getBounds().width()/2, imgView.getDrawable().getBounds().height()/2); 
		imgView.setImageMatrix(matrix);
	}

	private boolean loadImageList( View v, String[] picNameArray ) {
		LinearLayout totalLayout = new LinearLayout(this);
		totalLayout.setOrientation(LinearLayout.VERTICAL);

		//for ( String picName: picNameArray ) {
		for ( FoodItem item : mFoodItemList ) {
			//each layout item stored in a vertical linearLayout
			String picName = item.getPicName();
			LinearLayout linearLayout = new LinearLayout(this);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setMinimumHeight(300);
			ImageView imgView = new ImageView(this);
			loadImageFromPath(imgView,picName);
			TextView tv = new TextView(this);
			int i = 0;
			String comment = "NO COMMENTS";
			for ( i = 0; i < mCommentList.length; i++ ) {
				if ( mCommentList[i].getItemId() ==  item.getId() ) {
					comment = mCommentList[i].getContent();
				}
			}
			//tv.setText("this is image"+picName+'\n'+comment);
			if ( null != comment && comment.length() > 0 ) {
				tv.setText("this is image"+picName+'\n'+comment);
			} else {
				tv.setText("");
			}

			linearLayout.addView(imgView);
			linearLayout.addView(tv);
			totalLayout.addView(linearLayout);
			//add it to the listview lv
		}

		((ViewGroup) v).addView(totalLayout);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

