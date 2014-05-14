package com.example.eatanywhere;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	public String mServerResultString="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void cancelLogin( View v ) {
		finish();
	}
	
	public void login( View v ) {
		String loginName = ((EditText)findViewById(R.id.login_loginName)).getText().toString();
		String password = ((EditText)findViewById(R.id.login_password)).getText().toString();

	
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("loginName", loginName));
		nameValuePairs.add(new BasicNameValuePair("password", password));
		
		PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/login" );
		MyNetworkTask netTask = new MyNetworkTask(pe){
			@Override
			public void postHook() {
				mServerResultString = postNameValuePairs();
			}

			@Override
			public void afterPost() {
				LoginActivity.this.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							if ( isLoginSucceeded() ) {
								Toast.makeText( LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
								finish();
							}
							else {
								showLoginFailReason();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							
						
					}

				
				});
			}
		};
		netTask.execute("");
		
	}

	private void showLoginFailReason() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "用户名不存在或密码错误", Toast.LENGTH_SHORT).show();
	}

	private boolean isLoginSucceeded() throws JSONException {
		// TODO Auto-generated method stub
		JSONObject obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
		int success = Integer.parseInt(obj.getString("s"));
		return 1 == success;
	}

	public void register( View v ) {
		Intent it = new Intent();
		it.setClass(LoginActivity.this, RegisterActivity.class);
		startActivity(it);
		finish();
	}

}
