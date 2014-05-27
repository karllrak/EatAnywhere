package com.example.eatanywhere;

import java.text.ParseException;

import com.example.eatanywhere.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SelectEatingPlaceActivity extends Activity {
	public static String mServerIp = "10.254.239.1:8080";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selecteatingplace);
		
		Button btn = (Button) findViewById(R.id.relo);

		
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent it = new Intent();
				it.setClass(SelectEatingPlaceActivity.this, LoginActivity.class);
				startActivity(it);
			}});
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_eating_placeoeat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent it = null;
		switch ( item.getItemId() ) {
		case R.id.main_menu:
			it = new Intent(SelectEatingPlaceActivity.this, Memo.class);
			break;
		case R.id.login_menu:
			it = new Intent(SelectEatingPlaceActivity.this, LoginActivity.class);
			break;
		}
		if ( null != it ) {
			startActivity(it);
		}
		return super.onOptionsItemSelected(item);
	}
	*/
	
	public void one( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "一食堂");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}
	public void two( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "二食堂");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}	
	public void three( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "三食堂");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}
}
