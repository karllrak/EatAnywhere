package com.example.eatanywhere;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Memo extends ListActivity {
	private ListView listView;
	private CursorAdapter adapter;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView = getListView();
		
		String[] from = new String[] { "content" };
		int[] to = new int[] {R.id.contentTextView};
		adapter = new SimpleCursorAdapter(Memo.this, R.layout.content_list_item, null, from, to);
		setListAdapter(adapter);
		
	}
	@Override
	public void onResume() {
		super.onResume();
		new GetContentTask().execute((Object[]) null);
	}
	
	private class GetContentTask extends AsyncTask<Object, Object, Cursor> {
		DatabaseConnector connector =
				new DatabaseConnector(Memo.this);
		
		@Override
		protected Cursor doInBackground(Object... params) {
			connector.open();
			return connector.getAllContents();
		}
		@Override
		protected void onPostExecute(Cursor result) {
			adapter.changeCursor(result);
			connector.close();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.memo_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.memo_menu:
			Intent addNewContent = new Intent(Memo.this, Add.class);
			startActivity(addNewContent);
			return super.onOptionsItemSelected(item);
		case R.id.memo_menu2:
			clearAll();
			return true;
		}
			return true;
	}
	
	   private void clearAll() {
	      
	      AlertDialog.Builder builder = 
	         new AlertDialog.Builder(Memo.this);

	      builder.setTitle("Clear All?"); 
	      builder.setMessage("Clear All?"); 


	      builder.setPositiveButton("clear",
	         new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int button) {
	               final DatabaseConnector databaseConnector = 
	                  new DatabaseConnector(Memo.this);

	               AsyncTask<Object, Object, Object> deleteTask =
	                  new AsyncTask<Object, Object, Object>() {
	                     @Override
	                     protected Object doInBackground(Object... params)
	                     {
	                        databaseConnector.deleteAll(); 
	                        return null;
	                     } 

	                     @Override
	                     protected void onPostExecute(Object result)
	                     {
	                        onResume();
	                     } 
	                  }; 

	               
	               deleteTask.execute((Object[]) null);               
	            } 
	         } 
	      ); // end call to method setPositiveButton
	      
	      builder.setNegativeButton("cancel", null);
	      builder.show(); // display the Dialog
	   } // end method deleteContact

	
}