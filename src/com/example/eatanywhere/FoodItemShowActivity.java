package com.example.eatanywhere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

public class FoodItemShowActivity extends Activity {

	private FoodItem mFoodItem = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getFoodItem();
		setContentView(R.layout.fooditemshow);
		inflateFoodItem();
		super.onCreate(savedInstanceState);
	}

	private void inflateFoodItem() {
		// TODO Auto-generated method stub
		ImageView imageView = (ImageView) findViewById(R.id.fooditemshow_image);
		ImageLoader.loadImageFromPath(imageView, ListViewImageActivity.picDirPath+mFoodItem.getPicName());
		
		TextView txtplace = (TextView) findViewById(R.id.fooditemshow_place);
		txtplace.setText("地点："+mFoodItem.getPlace());
		
		String foodTime = mFoodItem.getCreatime();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date0 = null;
		try {
			date0 = sdf.parse(foodTime);
		} catch (ParseException e) {	
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date0);
		c.add(Calendar.MINUTE, 8 * 60);
		date0 = c.getTime();
		
		foodTime = sdf.format(date0);
		
		
		TextView txttime = (TextView) findViewById(R.id.fooditemshow_time);
		txttime.setText("时间："+foodTime);
		
		TextView txtuser = (TextView) findViewById(R.id.fooditemshow_user);
		txtuser.setText("用户："+mFoodItem.getUserId());
		
		TextView txtcomment = (TextView) findViewById(R.id.fooditemshow_comment);
		txtcomment.setText("评价："+mFoodItem.getComment());
		
	}

	private void getFoodItem() {
		// TODO Auto-generated method stub
		mFoodItem = (FoodItem) getIntent().getSerializableExtra("foodItem");
	}

	public void onToggleClicked(View view) {
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	    	uploadFoodItem();
	    } else {

	    }
	}	
	private void uploadFoodItem() {
		uploadFoodImage();
		uploadFoodInfo();
	}

	private void uploadFoodInfo() {
		// TODO Auto-generated method stub
		PostEntity pe = new PostEntity(null, null);
		MyNetworkTask netTask = new MyNetworkTask(pe) {
			public void postHook(){
				
			}
			public void afterPost() {
				
			}
		};
		netTask.execute("");
		
	}

	private void uploadFoodImage() {
		AsyncTask<String,Void,String> uploadImageTask = new AsyncTask<String,Void,String>(){
			protected String doInBackground(String... params) {
				String picFullName = ListViewImageActivity.picDirPath+mFoodItem.getPicName();
				MultipartEntity me = new MultipartEntity();
				try {
					//if we have loginned and has a token
					if ( null != LoginActivity.userToken && LoginActivity.userToken.length() > 0 
							&& null != LoginActivity.loginName && LoginActivity.loginName.length() > 0 ) {
						me.addPart("loginName", new StringBody(LoginActivity.loginName, Charset.forName("UTF-8")) );
						me.addPart("token", new StringBody(LoginActivity.userToken, Charset.forName("UTF-8")));
					}

					me.addPart("image", new FileBody(new File(picFullName)));
					me.addPart("picName", new StringBody(mFoodItem.getPicName(), Charset.forName("UTF-8") ));
					me.addPart("comment", new StringBody(mFoodItem.getComment(), Charset.forName("UTF-8") ));
					me.addPart("place", new StringBody(mFoodItem.getPlace(), Charset.forName("UTF-8") ));
					me.addPart("nono", new StringBody("yes", Charset.forName("UTF-8") ));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				AndroidHttpClient http_client = AndroidHttpClient.newInstance("karllrak");
				HttpPost post = null;
				post = new HttpPost( "http://"+SelectEatingPlaceActivity.mServerIp+"/uploadImage" );
				Log.e( "", "uploading image "+picFullName );

				post.setEntity( me );
				ResponseHandler<String> resHandler = new BasicResponseHandler();
				BasicHttpResponse response = null;
				String result = null;
				try {
					result = http_client.execute(post, resHandler);//, http_context );
					Log.e("", "http_client result"+result);
					return result;
				} catch (IOException e) {
					e.printStackTrace();
					result = "";
				} finally {
					http_client.close();
					return result;
				}	
			} 

			protected void onPostExecute(String result) {
				final String r = result;
				FoodItemShowActivity.this.runOnUiThread( new Runnable() {
					@Override
					public void run() {
						if ( r.equals("ok")) {
							Toast.makeText(FoodItemShowActivity.this, "同步照片成功", Toast.LENGTH_SHORT).show();
							ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
							btn.setChecked(true);
						}
						else {
							String failReason = LoginActivity.isLoginOrReasonString(r);
							if ( null != failReason ) {
								if ( 0 == failReason.length() ) {
									failReason = "网络无响应";
											}
								Toast.makeText(FoodItemShowActivity.this, failReason, Toast.LENGTH_SHORT).show();
								if ( failReason.equals("请登录" ) ) {
									Intent it = new Intent();
									it.setClass(FoodItemShowActivity.this, LoginActivity.class);
									startActivityForResult(it, 0);
								}
							}
							else {
								Toast.makeText(FoodItemShowActivity.this, "同步照片失败", Toast.LENGTH_SHORT).show();
							}
							ToggleButton btn = (ToggleButton) findViewById(R.id.fooditemshow_toggleButton);
							btn.setChecked(false);
						}
					}

				});
			}
		};
		uploadImageTask.execute("");
		
	}

	public void finishActivity( View v ) {
		//this is used for button click
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if ( 1 != resultCode ) {
			uploadFoodImage();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


}
