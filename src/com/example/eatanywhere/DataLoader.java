package com.example.eatanywhere;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class DataLoader {
	private static String mServerResultString = null;

	public static FoodItem[] getFoodItemListByPicnameArray(Context context, String[] picNameArray)
	{
		DatabaseConnector databaseConnector = new DatabaseConnector(context);
		databaseConnector.open();
		//load the picname first
		Cursor c = databaseConnector.database.query("foodItem", new String[] {"picName", "rowid","place","creatime"}, null, null, null, null, null);
		FoodItem[] foodItemList = new FoodItem[c.getCount()];
		for (int i = 0; i < foodItemList.length; i++ ) {
			foodItemList[i] = new FoodItem();
		}
		String tmpPicName = null;
		String place = null;
		String creatime = null;
		int i = 0;
		int rowid = 0;
		while ( c.moveToNext() ) {
			tmpPicName = c.getString(0);
			rowid = c.getInt(1);
			place = c.getString(2);
			creatime = c.getString(3);
			foodItemList[i].setPicName(tmpPicName);
			foodItemList[i].setId(rowid);
			foodItemList[i].setPlace(place);
			foodItemList[i].setCreatime(creatime);

			i++;
		}
		c.close();
		databaseConnector.close();
		return foodItemList;
	}

	public static FoodComment[] getPicCommentList(Context context) {
		//load the picture comment into mFoodComment
	//load the comments
		DatabaseConnector databaseConnector = new DatabaseConnector(context);
		databaseConnector.open();
		Cursor c = databaseConnector.database.query("foodComment", 
				new String[] {"itemId", "content","creatime"}, null, null, null, null, null);
		FoodComment[] commentList = new FoodComment[c.getCount()]; 
		for (int j = 0; j < commentList.length; j++ ) {
			commentList[j] = new FoodComment();
		}
		int i = 0;
		int tmpId = 0;
		String tmpContent = null;
		String creatime = null;
		while ( c.moveToNext() ) {
			tmpId = c.getInt(0);
			tmpContent = c.getString(1);
			creatime = c.getString(2);
			commentList[i].setContent(tmpContent);
			commentList[i].setItemId(tmpId);
			commentList[i].setCreatime(creatime);
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
				JSONObject scoreArray = null;
				try {
					scoreArray = obj.getJSONObject("scoreList");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int score = 0;
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
