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
import org.json.JSONObject;
import org.json.JSONTokener;

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
import android.view.LayoutInflater;
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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class PcvLayout extends LinearLayout {
	FoodItem mFoodItem = null;
	private String mServerResultString;
	private Activity mActivity = null;
	public PcvLayout(Context context) {
		super(context);
		mActivity = (Activity) context;
		LayoutInflater.from(context).inflate(R.layout.pcvlayout, this, true); 
		loadImage();
	}
		
	private void loadImage() {
		ImageView imgView = (ImageView) findViewById(R.id.playout);
		ImageLoader.loadImageFromPath(imgView,ListViewImageActivity.picDirPath+getPicName());
		imgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
					Intent it = new Intent();
					it.putExtra("foodItem", mFoodItem);
					it.setClass(mActivity, FoodItemShowActivity.class);
					mActivity.startActivity(it);
			}});
	}

	public String getPicName()
	{
		return mFoodItem.getPicName();
	}

	public PcvLayout(Context context, FoodItem foodItem) {
		super(context);
		mActivity = (Activity) context;
		mFoodItem = foodItem;
		LayoutInflater.from(context).inflate(R.layout.pcvlayout, this, true); 
		loadImage();
		
		Button up = (Button) findViewById(R.id.bup);
		up.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
				nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));
				nameValuePairs.add(new BasicNameValuePair("picName", getPicName()));
				nameValuePairs.add(new BasicNameValuePair("type", "1"));

				PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/vote" );
				MyNetworkTask netTask = new MyNetworkTask(pe){
					@Override
					public void postHook() {
						mServerResultString = postNameValuePairs();
					}

					@Override
					public void afterPost() {
						mActivity.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									onVoteResult();
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							

							}

							
							});
						}
					};
					netTask.execute("");
				}});
			
			
			TextView vnumber = (TextView) findViewById(R.id.vnumber);
			vnumber.setText("0");		
			
			Button down = (Button) findViewById(R.id.bdown);		
			
			down.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
					nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));
					nameValuePairs.add(new BasicNameValuePair("picName", getPicName()));
					nameValuePairs.add(new BasicNameValuePair("type", "-1"));
					
					PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/vote" );
					MyNetworkTask netTask = new MyNetworkTask(pe){
						@Override
						public void postHook() {
							mServerResultString = postNameValuePairs();
						}
						

						@Override
						public void afterPost() {
							mActivity.runOnUiThread(new Runnable(){
								@Override
								public void run() {
									try {
										onVoteResult();
									} catch (NumberFormatException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					};
					netTask.execute("");
				}});
		
	}

	private void onVoteResult() throws NumberFormatException, JSONException
	{
		//
		JSONObject obj = null;
		try {
			obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
		}
		catch ( JSONException e ) {
			//not a json string
			e.printStackTrace();
			String fail= "网络无响应";
			Toast.makeText(mActivity, fail, Toast.LENGTH_SHORT).show();
			return;
		}

	
		//test if we have loginned!
		String failReason = LoginActivity.isLoginOrReasonString(mServerResultString);
		if ( null != failReason ) {
			if ( 0 == failReason.length() ) {
				failReason = "网络无响应";
				Toast.makeText(mActivity, failReason, Toast.LENGTH_SHORT).show();
			}
			if ( failReason.equals("请登录" ) ) {
				Intent it = new Intent();
				it.setClass(mActivity, LoginActivity.class);
				mActivity.startActivityForResult(it, 0);
			}
		}
		
			//test if server return success { 's':1 }
		int success = Integer.parseInt( obj.getString("s") );
		if ( 0 == success ) {
			//Oops! we failed!
			Toast.makeText(mActivity, "评分失败 "+obj.getString("err"), Toast.LENGTH_SHORT).show();
			return;
		}
		String voteSum = obj.getString("voteScore");
		TextView vnumber = (TextView) findViewById(R.id.vnumber);
		vnumber.setText(voteSum);		
	
		Toast.makeText(mActivity, "评分成功", Toast.LENGTH_SHORT).show();

	}
}
