package com.example.eatanywhere;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
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
	public static String picDirPath=Environment.getExternalStorageDirectory()+"/EatPic/";
	private String picFileType="jpeg";
	private HashMap mItemList;
	private FoodItem[] mFoodItemList=null;
	private FoodComment[] mCommentList=null;
	private ScrollView mScrollView=null;
	private String placeToEat=null;
	private ArrayList<PcvLayout> mPcvLayoutList = new ArrayList<PcvLayout>();
	private String mServerResultString = "";
	private String[] mPicFullPathNameArrayInSDCard=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//create the dir for saving pics
		File createDirOrExist = new File(picDirPath);
		createDirOrExist.mkdir();

		//显示从哪个食堂点击进来的
		Intent it = getIntent();
		placeToEat = it.getStringExtra("placeToEat");

		mScrollView = new ScrollView(this);
		mPicFullPathNameArrayInSDCard = getPicNamePathList();
		mFoodItemList = DataLoader.getFoodItemListByPicnameArray(this, mPicFullPathNameArrayInSDCard);
		mCommentList = DataLoader.getPicCommentList(this);

		LinearLayout totalLayout = new LinearLayout(this);
		totalLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout topTxtLayout = new LinearLayout(this);
		topTxtLayout.setGravity(Gravity.CENTER);
		

		LinearLayout topBtnLayout = new LinearLayout(this);

			
		TextView tv = new TextView(this);
		tv.setTextSize(20);
		if ( placeToEat.equals("一食堂") ) {
			tv.setText("第一食堂");
		} else if ( placeToEat.equals("二食堂") ) {
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
				it.putExtra("placeToEat", placeToEat);
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
				try {
					reload();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		try {
			loadImageList(imageLayout, mPicFullPathNameArrayInSDCard);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		totalLayout.addView(topTxtLayout);
		totalLayout.addView(topBtnLayout);
		totalLayout.addView(imageLayout);
		totalLayout.setBackgroundColor(Color.YELLOW);
		mScrollView.addView(totalLayout);
		//setContentView(R.layout.activity_main);
		setContentView(mScrollView);
		DataLoader.loadScoreFromNetWork(this, mPcvLayoutList);
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

	private void reload() throws ParseException{
		mPicFullPathNameArrayInSDCard = getPicNamePathList();
		mFoodItemList = DataLoader.getFoodItemListByPicnameArray(this, mPicFullPathNameArrayInSDCard);
		mCommentList = DataLoader.getPicCommentList(this);

		LinearLayout imageLayout = (LinearLayout) mScrollView.getChildAt(0);
		imageLayout = (LinearLayout) imageLayout.getChildAt(2);
		imageLayout.removeAllViews();
		String[] newPicNameList = getPicNamePathList();
		loadImageList(imageLayout,newPicNameList);
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

	private boolean loadImageList( View v, String[] picNameArray ) throws ParseException {
		LinearLayout totalLayout = new LinearLayout(this);
		totalLayout.setOrientation(LinearLayout.VERTICAL);

		//for ( String picName: picNameArray ) {
		for ( FoodItem item : mFoodItemList ) {
			//each layout item stored in a vertical linearLayout
			final String picName = item.getPicName();

			//not the right place?
			if (!placeToEat.equals(item.getPlace())) {
				continue;
			}

			//no such picture?
			boolean picNameFound = false;
			for ( int i = 0; i < mPicFullPathNameArrayInSDCard.length; i++ ) {
				if ( mPicFullPathNameArrayInSDCard[i].equals(picName)){
					picNameFound = true;
				}
			}
			if (!picNameFound) {
				continue;
			}

			String place = item.getPlace();
			
			PcvLayout layout = new PcvLayout(this, item);
			mPcvLayoutList.add(layout);
		
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
			time.setText( DataLoader.betterTimeShow(item.getCreatime()) );

			
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

