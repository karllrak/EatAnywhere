package com.example.eatanywhere;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.widget.Toast;

public class DataLoader {
	private static String mServerResultString = null;
	private static InputStream picIn = null;
	private static OutputStream outPicStream = null;
	private static ArrayList<FoodItem> mFoodItemList = null;

	public static ArrayList<FoodItem> getFoodItemListByPicnameArray(Context context, String[] picNameArray, ArrayList<FoodItem> foodItemList)
	{
		DatabaseConnector databaseConnector = new DatabaseConnector(context);
		databaseConnector.open();
		//load the picname first
		Cursor c = databaseConnector.database.query("foodItem", new String[] {"picName", "rowid","place","creatime"}, null, null, null, null, null);
		String tmpPicName = null;
		String place = null;
		String creatime = null;
		int i = 0;
		int rowid = 0;
		while ( c.moveToNext() ) {
			FoodItem foodItem = new FoodItem();
			tmpPicName = c.getString(0);
			rowid = c.getInt(1);
			place = c.getString(2);
			creatime = c.getString(3);
			foodItem.setPicName(tmpPicName);
			foodItem.setId(rowid);
			foodItem.setPlace(place);
			foodItem.setLocal();
			foodItem.setCreatime(creatime);
			foodItem.setUserId(LoginActivity.mLoginName);
			boolean found = false;
			for ( FoodItem item: foodItemList) {
				if ( item.getPicName().equals(tmpPicName)) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				foodItemList.add(foodItem);
			}

			i++;
		}
		c.close();
		databaseConnector.close();
		loadItemListFromNetWork(context, foodItemList);
		return foodItemList;
	}

	public static boolean picExistsInSDCard(String picName ) {
		Log.e("", "exist? "+picName);
		File createDirOrExist = new File(ListViewImageActivity.picDirPath+picName);
		return createDirOrExist.exists();
	}
	public static void loadPicFromNetworkToPcvList(final Context context, final ArrayList<FoodItem> itemList ) {
		for ( int i = 0; i < itemList.size(); i++ ) {
			FoodItem item = itemList.get(i);
			final String picName = item.getPicName();
			if( !picExistsInSDCard(picName) ) {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
				nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));
				nameValuePairs.add(new BasicNameValuePair("picName", picName));


				final PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/loadItemList" );
				MyNetworkTask netTask = new MyNetworkTask(pe){
					private ListViewImageActivity act = (ListViewImageActivity) context;

					@Override
					public void postHook() {
						picIn = null;
						AndroidHttpClient http_client = AndroidHttpClient.newInstance("karllrak");
						HttpPost post = null;
						String postUrl = "http://"+SelectEatingPlaceActivity.mServerIp+"/loadImage";
						post = new HttpPost( postUrl );
						Log.e( "", "info: new post "+ postUrl );

						try {
							post.setEntity( (HttpEntity) new UrlEncodedFormEntity((List<? extends NameValuePair>) pe.getPairList(), HTTP.UTF_8) );
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						HttpResponse response = null;
						try {
							response = http_client.execute(post);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							if ( null != response ) {
								FileOutputStream file = new FileOutputStream(ListViewImageActivity.picDirPath+picName);
								response.getEntity().writeTo(file);
								http_client.close();
								//picIn = ((HttpResponse) ((HttpEntity) response).getContent()).getEntity().getContent();
							}
							else {
								http_client.close();
								Log.e("", "server response null when post "+postUrl);
							}
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							http_client.close();
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							http_client.close();
							e.printStackTrace();
						}
					}

					@Override
					public void afterPost() {
						//writeStreamToLocalFile(picIn, picName);
					}
				};
				netTask.execute("");

			}
		}
	}

	public static void writeStreamToLocalFile(InputStream in, String picName ) {
		if ( null == in ) {
			Log.e("", "writeStreamToLocalFile get InputStream null! with picName= "+picName );
			return;
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        try {
			while( (len=in.read(buffer)) != -1){  
			    outStream.write(buffer, 0, len);  
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
        try {
			outStream.close();
			outPicStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
        try {
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
        byte[] picData = ((ByteArrayOutputStream) outPicStream).toByteArray();
		Bitmap bmp = BitmapFactory.decodeByteArray(picData, 0, picData.length);

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(ListViewImageActivity.picDirPath+picName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	public static void loadItemListFromNetWork(final Context context, ArrayList<FoodItem> foodItemList) {
		mFoodItemList = foodItemList;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
		nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));


		PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/loadItemList" );
		MyNetworkTask netTask = new MyNetworkTask(pe){
			@Override
			public void postHook() {
				mServerResultString = "";
				mServerResultString = postNameValuePairs();
			}

			@Override
			public void afterPost() {
				JSONObject obj = null;
				try {
					obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
				}
				catch ( JSONException e ) {
					//not a json string
					e.printStackTrace();
					String fail= "网络无响应";
					Log.e("", fail );
					return;
				}

				//test if we have loginned!
				String failReason = LoginActivity.isLoginOrReasonString(mServerResultString);
				if ( null != failReason ) {
					if ( 0 == failReason.length() ) {
						failReason = "网络无响应";
						Log.e("", failReason );
					}
					if ( failReason.equals("请登录" ) ) {
					((Activity) context).runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(context,"登录可以查看评分", Toast.LENGTH_SHORT).show();
						} } );
						return;
					}
				}


				int success = 0;

				try {
					success = Integer.parseInt( obj.getString("s") );
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ( 0 == success ) {
					//Oops! we failed!
					Log.e("", "loadScore get s=0");
					return;
				}
				
				JSONArray itemArray = null;
				try {
					itemArray = obj.getJSONArray("itemArray");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				boolean found = false;
				if ( null == itemArray ) {
					return;
				}
				for ( int i = 0; i < itemArray.length(); i++ ) {
					JSONObject o = (JSONObject) itemArray.opt(i);
					FoodItem item = jsonObjectToFoodItem(o);
					final ListViewImageActivity act = (ListViewImageActivity) context;
					//act.mFoodItemList.add(item);
					for ( FoodItem localItem: mFoodItemList) {
						if ( item.getPicName().equals(localItem.getPicName())) {
							found = true;
							break;
						}
					}
					if ( !found ) {
						mFoodItemList.add(item);
					}
					found = false;
					Log.e("", "add one item to mFoodItemList");
				}

				//then we load the picTures
			}
		};
		netTask.execute("");

	}

	public static FoodItem jsonObjectToFoodItem(JSONObject o){
		Iterator<String> iter = o.keys();
		String key = null;
		String value = null;
		FoodItem foodItem = new FoodItem();
		while( iter.hasNext() ) {
			key = iter.next();
			try {
				value = o.getString(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ( key.equals("picName") ) {
				foodItem.setPicName(value);
				continue;
			}
			if ( key.equals("comment")) {
				foodItem.setComment(value);
				continue;
			}
			if ( key.equals("loginName")){
				foodItem.setUserId(value);
				continue;
			}
			if (key.equals("creatime")) {
				foodItem.setCreatime(value);
				continue;
			}
			if ( key.equals("place") ) {
				foodItem.setPlace(value);
				continue;
			}
		}
		return foodItem;
	}
	public static ArrayList<FoodComment> getPicCommentList(Context context) {
		//load the picture comment into mFoodComment
	//load the comments
		DatabaseConnector databaseConnector = new DatabaseConnector(context);
		databaseConnector.open();
		Cursor c = databaseConnector.database.query("foodComment", 
				new String[] {"itemId", "content","creatime"}, null, null, null, null, null);
		ArrayList<FoodComment> commentList = new ArrayList<FoodComment>();

		int i = 0;
		int tmpId = 0;
		String tmpContent = null;
		String creatime = null;
		while ( c.moveToNext() ) {
			FoodComment comment = new FoodComment();
			tmpId = c.getInt(0);
			tmpContent = c.getString(1);
			creatime = c.getString(2);
			comment.setContent(tmpContent);
			comment.setItemId(tmpId);
			comment.setCreatime(creatime);
			commentList.add(comment);
			i++;
		}
		c.close();
		databaseConnector.close();
		return commentList;
	}
	public static void loadScoreFromNetWork(final Context context, final ArrayList<PcvLayout> dscList ) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("loginName", LoginActivity.loginName));
		nameValuePairs.add(new BasicNameValuePair("token", LoginActivity.userToken));

		PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/loadScore" );
		MyNetworkTask netTask = new MyNetworkTask(pe){
			@Override
			public void postHook() {
				mServerResultString = "";
				mServerResultString = postNameValuePairs();
			}

			@Override
			public void afterPost() {
				JSONObject obj = null;
				try {
					obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
				}
				catch ( JSONException e ) {
					//not a json string
					e.printStackTrace();
					String fail= "网络无响应";
					Toast.makeText(context,fail, Toast.LENGTH_SHORT).show();
					Log.e("", fail );
					return;
				}

				//test if we have loginned!
				String failReason = LoginActivity.isLoginOrReasonString(mServerResultString);
				if ( null != failReason ) {
					if ( 0 == failReason.length() ) {
						failReason = "网络无响应";
						Toast.makeText(context,failReason, Toast.LENGTH_SHORT).show();
						Log.e("", failReason );
					}
					if ( failReason.equals("请登录" ) ) {
					((Activity) context).runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(context,"登录可以查看更多", Toast.LENGTH_SHORT).show();
						} } );
						return;
					}
				}
				
				//get s = 0 ? we failed？
				int success = 0;
				try {
					success = Integer.parseInt( obj.getString("s") );
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ( 0 == success ) {
					//Oops! we failed!
					Log.e("", "loadScore get s=0");
					return;
				}

				JSONObject scoreArray = null;
				try {
					scoreArray = obj.getJSONObject("scoreList");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int score = 0;
				if ( null == scoreArray ) {
					return;
				}
				Iterator<String> iter = scoreArray.keys();
				while ( iter.hasNext() ) {
					String key = iter.next();
					PcvLayout layout = findPcvLayoutInListByPicName(dscList, key);
					if ( null != layout ) {
						try {
							score = Integer.parseInt(scoreArray.getString(key));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						layout.mFoodItem.setScore(score);
						layout.updateScore();
					}
				}
			}
		};
		netTask.execute("");
	}

	public static PcvLayout findPcvLayoutInListByPicName(ArrayList<PcvLayout> list, String picName) {
		Iterator<PcvLayout> iter = list.iterator();
		PcvLayout found = null;
		while ( iter.hasNext() ) {
			PcvLayout layout = iter.next();
			if ( null == layout.mFoodItem ) {
				continue;
			}
			if ( picName.equals(layout.mFoodItem.getPicName()) ) {
				found = layout;
			}
		}
		return found;
	}

public static String betterTimeShow(String creatime) throws ParseException {
	// TODO Auto-generated method stub
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		Calendar cal = Calendar.getInstance();
		//东八区！
		cal.add(Calendar.HOUR_OF_DAY, -8);
		Date date = (Date) format.parse(creatime);
		Date currentDate =  (Date) cal.getTime();
		if( date.getYear() != currentDate.getYear() ){
			return ""+(currentDate.getYear()-date.getYear())+"年前";
		}
		//ok the same year!
		if( date.getMonth() != currentDate.getMonth() ){
			return ""+(currentDate.getMonth()-date.getMonth())+"个月前";
		}
		//ok the same month
		if( date.getDate() != currentDate.getDate() ){
			return ""+(currentDate.getDate()-date.getDate())+"天前";
		}
		// the same date
		if( date.getHours() != currentDate.getHours() ) {
			return ""+(currentDate.getHours()-date.getHours())+"小时前";
		}
		//the same hour
		if( date.getMinutes() != currentDate.getMinutes() ) {
			return ""+(currentDate.getMinutes()-date.getMinutes())+"分钟前";
		}
		//the same minute
		if( date.getSeconds() != currentDate.getSeconds() ){
			return ""+(currentDate.getSeconds()-date.getSeconds())+"秒前";
		}

		return "不可能！你怎么看到的！";
	}

}