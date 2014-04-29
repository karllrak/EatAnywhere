package com.example.eatanywhere;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class SelectEatingPlaceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selecteatingplace);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_eating_placeoeat, menu);
		return true;
	}
	
	public void one( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "one");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}
	public void two( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "two");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}	
	public void three( View v ) {
		Intent it = new Intent();
		it.putExtra("placeToEat", "three");
		it.setClass(SelectEatingPlaceActivity.this, ListViewImageActivity.class);
		startActivity(it);
	}
}
