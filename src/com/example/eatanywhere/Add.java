package com.example.eatanywhere;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;

public class Add extends Activity {

	private long rowID;
	
	private EditText contentEditText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_content);
		contentEditText = (EditText) findViewById(R.id.contentTextEdit);
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			rowID = extras.getLong("row_id");
			contentEditText.setText(extras.getString("content"));
		}
		
		Button saveContentButton = (Button) findViewById(R.id.relo);
		saveContentButton.setOnClickListener(saveContentButtonClicked);
	}
	
	   OnClickListener saveContentButtonClicked = new OnClickListener() 
	   {
	      @Override
	      public void onClick(View v) {
	         if (contentEditText.getText().length() != 0) {
	            AsyncTask<Object, Object, Object> saveContactTask = 
	               new AsyncTask<Object, Object, Object>() {
	                  @Override
	                  protected Object doInBackground(Object... params) {
	                     saveContact(); 
	                     return null;
	                  } 
	      
	                  @Override
	                  protected void onPostExecute(Object result) {
	                     finish(); 
	                  } 
	               }; 
	               
	            saveContactTask.execute((Object[]) null); 
	         } else {
	            
	            AlertDialog.Builder builder = 
	               new AlertDialog.Builder(Add.this);
	      
	           
	            builder.setTitle("error"); 
	            builder.setMessage("error");
	            builder.setPositiveButton("error", null); 
	            builder.show();
	         } 
	      } 
	   };

	   private void saveContact() {
	      // get DatabaseConnector to interact with the SQLite database
	      DatabaseConnector databaseConnector = new DatabaseConnector(this);

	      if (getIntent().getExtras() == null) {
	         // insert the contact information into the database
	         databaseConnector.insertContent(
	            contentEditText.getText().toString());
	      } else {
	         //databaseConnector.updateContent(rowID,contentEditText.getText().toString());
	      } 
	   } 

}
