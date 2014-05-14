package com.example.eatanywhere;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Toast;


/**
 * Use new each time you want a post
 * implement the post_hook() which is called during the post
 * and the af_post() which deals with the post result
 * USAGE: MyNetworkTask xxx = new NetworkTask([PostEntity]){
 * 	public void postHook(){
 * 		//you codes here
 * 		mResultString = postNameValuePairs();
 * 	}
 * 	public void afterPost() {
 * 		//you codes deals with the post result here
 * 	}
 * }
 *	xxx.execute( "the string can be empty" ); 
 */
public abstract class MyNetworkTask extends AsyncTask<String, Void, String> {
	private static Activity mMainActivity = null;
	private boolean mbPostPairsOnly = false;
	private String post_result = null;
	private String mtarget_url = null;
	private PostEntity mEntityPost = null;


	public static void setActivity( Activity act )
	{
		mMainActivity = act;
	}
	public void setPostPairsOnly()
	{
		mbPostPairsOnly = true;
	}
	
	public abstract void postHook();
	public abstract void afterPost();
	
	public MyNetworkTask()
	{
	}
	public MyNetworkTask( PostEntity pe )
	{
		mEntityPost = pe;
	}
	/**
	 * a template route for network tasks 
	 * warning: the post cannot deal with large string
	 * @param params
	 * 	the name value pair list to send
	 * @return
	 *	the result String 
	 */
	public String postNameValuePairs()
	{
		String str_target_ip = mEntityPost.getTargetUrl();
		if ( str_target_ip.matches("") )
		{
			str_target_ip = "192.168.0.1:8080";
		}
		
		AndroidHttpClient http_client = AndroidHttpClient.newInstance("karllrak");
		HttpPost post = null;
		if ( null == mtarget_url ) {
			post = new HttpPost( "http://"+str_target_ip );
			Log.e( "", "info: new post"+ str_target_ip );
		} else {
			post = new HttpPost( mtarget_url );
			Log.e( "", "info: new post"+ mtarget_url );
		}
		
		try {
			post.setEntity( (HttpEntity) new UrlEncodedFormEntity((List<? extends NameValuePair>) mEntityPost.getPairList(), HTTP.UTF_8) );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ResponseHandler<String> res = new BasicResponseHandler();
		BasicHttpResponse response = null;
		String result = null;
		try {
			result = http_client.execute(post, res);//, http_context );
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
	
	@Override
	protected String doInBackground(String... arg0) {
		String mtarget_url = arg0.toString();
		postHook();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		afterPost();
	}
}

class PostEntity {
	private List<NameValuePair> pairList = null;
	private String strTargetIp = null;
	public PostEntity(List<NameValuePair> l, String url) {
		pairList = l;
		strTargetIp = url;
	}
	public List<NameValuePair> getPairList(){
		return pairList;
	}

	public String getTargetUrl() {
		return strTargetIp;
	}
}
