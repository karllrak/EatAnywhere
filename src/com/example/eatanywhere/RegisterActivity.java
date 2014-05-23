package com.example.eatanywhere;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private String mServerResultString = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void register( View v ) {
		String loginName = ((EditText)findViewById(R.id.loginName)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		String passAgain = ((EditText)findViewById(R.id.passAgain)).getText().toString();

		if ( !password.equals(passAgain) ) {
			Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
			return;
		}
	
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("loginName", loginName));
		nameValuePairs.add(new BasicNameValuePair("password", password));
		
		PostEntity pe = new PostEntity(nameValuePairs, SelectEatingPlaceActivity.mServerIp+"/register" );
		MyNetworkTask netTask = new MyNetworkTask(pe){
			@Override
			public void postHook() {
				mServerResultString = postNameValuePairs();
			}

			@Override
			public void afterPost() {
				RegisterActivity.this.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							if ( isRegisterSucceeded() ) {
								Toast.makeText( RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
								finish();
							}
							else {
								showRegisterFailReason();
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

	public void showRegisterFailReason() throws JSONException {
		JSONObject obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
		String reason = obj.getString("err");
		if (reason.indexOf("already") != -1 ) {
			//用户名已存在
			Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, "注册失败 "+reason, Toast.LENGTH_SHORT).show();
		}
	}

	public boolean isRegisterSucceeded() throws JSONException {
		//TODO
		JSONObject obj = (JSONObject) new JSONTokener(mServerResultString).nextValue();
		int success = Integer.parseInt(obj.getString("s"));
		return 1 == success;
	}
	public void cancelRegister( View v ) {
		finish();
	}

}
