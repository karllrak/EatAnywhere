package com.example.eatanywhere;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
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
	private String mServerResultString;

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
		LinearLayout topTxtLayout = new LinearLayout(this);
		topTxtLayout.setGravity(Gravity.CENTER);
		

		LinearLayout topBtnLayout = new LinearLayout(this);

			
		TextView tv = new TextView(this);
		tv.setTextSize(20);
		if ( placeToEat.equals("one") ) {
			tv.setText("第一食堂");
		} else if ( placeToEat.equals("two") ) {
			tv.setText("第二食堂");
		} else {
			tv.setText("第三食堂");
		}

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(5, 10, 0, 0);
		
		
		Button btn = new Button(this);

		btn.setBackgroundResource(R.drawable.shape1);
		btn.setLayoutParams(lp);
		
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent it = new Intent();
				it.setClass(ListViewImageActivity.this, CameraCapture.class);
				startActivity(it);
			}});
		btn.setText("开始上传");
		
		Button btnReload = new Button(this);

		btnReload.setText("刷新");
		btnReload.setBackgroundResource(R.drawable.shape1);
		btnReload.setLayoutParams(lp);
		
		btnReload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				reload();
			}});
		
		Spinner sp = new Spinner(this);
		String[] spitem = new String[] {"按时间排序", "按评分排序"};		
		ArrayAdapter<String> spAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spitem);
		sp.setAdapter(spAdapter);
		TextView txtb = new TextView(this);
		txtb.setText("          ");

		
		topTxtLayout.addView(tv);
		topBtnLayout.addView(btn);
		topBtnLayout.addView(btnReload);
		topBtnLayout.addView(txtb);
		topBtnLayout.addView(sp);
		
		
		LinearLayout imageLayout = new LinearLayout(this);
		imageLayout.setOrientation(LinearLayout.VERTICAL);
		loadImageList(imageLayout, picFullPathNameArray);

		totalLayout.addView(topTxtLayout);
		totalLayout.addView(topBtnLayout);
		totalLayout.addView(imageLayout);
		totalLayout.setBackgroundColor(Color.YELLOW);
		mScrollView.addView(totalLayout);
		//setContentView(R.layout.activity_main);
		setContentView(mScrollView);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//mScrollView = null;
		super.onStop();
	}

	private void reload(){
		getPicCommentList();
		LinearLayout imageLayout = (LinearLayout) mScrollView.getChildAt(0);
		imageLayout = (LinearLayout) imageLayout.getChildAt(2);
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
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inScaled = true;
			opt.inTargetDensity = opt.inScreenDensity / 30;
			Bitmap bmp = BitmapFactory.decodeStream(fin, null, opt);
			imgView.setImageBitmap(bmp);
			imgView.setAdjustViewBounds(true);
			imgView.setMaxHeight(300);
			imgView.setMaxWidth(150);
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
			final String picName = item.getPicName();
			String place = item.getPlace();
			
			PcvLayout layout = new PcvLayout(this);

			Button up = (Button) layout.findViewById(R.id.bup);
			
			up.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
					nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));
					nameValuePairs.add(new BasicNameValuePair("picName", picName));
					nameValuePairs.add(new BasicNameValuePair("type", "1"));
					
					PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/vote" );
					MyNetworkTask netTask = new MyNetworkTask(pe){
						@Override
						public void postHook() {
							mServerResultString = postNameValuePairs();
						}
						

						@Override
						public void afterPost() {
							ListViewImageActivity.this.runOnUiThread(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									if ( mServerResultString.equals("ok")) {
										Toast.makeText(ListViewImageActivity.this, "同步照片成功", Toast.LENGTH_SHORT).show();
										ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
										btn.setChecked(true);
									}
									else {
										String failReason = LoginActivity.isLoginOrReasonString(mServerResultString);
										if ( null != failReason ) {
											Toast.makeText(ListViewImageActivity.this, failReason, Toast.LENGTH_SHORT).show();
											if ( failReason.equals("请登录" ) ) {
												Intent it = new Intent();
												it.setClass(ListViewImageActivity.this, LoginActivity.class);
												startActivityForResult(it, 0);
											}
										}
										else {
											Toast.makeText(ListViewImageActivity.this, "同步照片失败", Toast.LENGTH_SHORT).show();
										}
										ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
										btn.setChecked(false);
									}
										
									
								}

							
							});
						}
					};
					netTask.execute("");
				}});
			
			
			TextView vnumber = (TextView) layout.findViewById(R.id.vnumber);
			vnumber.setText("0");		
			
			Button down = (Button) layout.findViewById(R.id.bdown);		
			
			down.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
					nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));
					nameValuePairs.add(new BasicNameValuePair("picName", picName));
					nameValuePairs.add(new BasicNameValuePair("type", "-1"));
					
					PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/vote" );
					MyNetworkTask netTask = new MyNetworkTask(pe){
						@Override
						public void postHook() {
							mServerResultString = postNameValuePairs();
						}
						

						@Override
						public void afterPost() {
							ListViewImageActivity.this.runOnUiThread(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									if ( mServerResultString.equals("ok")) {
										Toast.makeText(ListViewImageActivity.this, "同步照片成功", Toast.LENGTH_SHORT).show();
										ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
										btn.setChecked(true);
									}
									else {
										String failReason = LoginActivity.isLoginOrReasonString(mServerResultString);
										if ( null != failReason ) {
											Toast.makeText(ListViewImageActivity.this, failReason, Toast.LENGTH_SHORT).show();
											if ( failReason.equals("请登录" ) ) {
												Intent it = new Intent();
												it.setClass(ListViewImageActivity.this, LoginActivity.class);
												startActivityForResult(it, 0);
											}
										}
										else {
											Toast.makeText(ListViewImageActivity.this, "同步照片失败", Toast.LENGTH_SHORT).show();
										}
										ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
										btn.setChecked(false);
									}
										
									
								}

							
							});
						}
					};
					netTask.execute("");
				}});
	
			ImageView imgView = (ImageView) layout.findViewById(R.id.playout);
			loadImageFromPath(imgView,picName);
			imgView.setOnClickListener(new MyOnClick(item));
			
			TextView tv1 = (TextView) layout.findViewById(R.id.comment);
			int i = 0;
			String comment = "NO COMMENTS";
			for ( i = 0; i < mCommentList.length; i++ ) {
				if ( mCommentList[i].getItemId() ==  item.getId() ) {
					comment = mCommentList[i].getContent();
				}
			}
			//tv.setText("this is image"+picName+'\n'+comment);
			if ( null != comment && comment.length() > 0 ) {
				tv1.setText(comment);
			} else {
				tv1.setText("");
			}			
			tv1.setOnClickListener(new MyOnClick(item));
			
			TextView user = (TextView) layout.findViewById(R.id.user);
			user.setText("local");
			TextView time = (TextView) layout.findViewById(R.id.time);
			time.setText("just now");

			
			totalLayout.addView(layout);
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
	private class MyOnClick implements OnClickListener{
		private FoodItem f;
		
		public MyOnClick(FoodItem _f) {
			f = _f;
		}
		
		@Override
		public void onClick(View v) {
			Intent it = new Intent();
			it.putExtra("foodItem", f);
			it.setClass(ListViewImageActivity.this, FoodItemShowActivity.class);
			startActivity(it);
		}
	}

}

