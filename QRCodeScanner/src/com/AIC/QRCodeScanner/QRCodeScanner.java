package com.AIC.QRCodeScanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.AIC.QRCodeScanner.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.provider.Settings.Secure;


public class QRCodeScanner extends Activity {
	
	private static final String LSBuildingsDB = null;
	private static final String androidBeta = null;
	
	
	//constants for the camera
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private String filePath;
	
	
	DataBaseHelper myDbHelper = new DataBaseHelper(null);
	
	List<QueryLister> model = new ArrayList<QueryLister>();
	QueryLister cl = new QueryLister();
	
	String newString = null;

	File sdcard = Environment.getExternalStorageDirectory();
	File file = new File(sdcard,"state.txt");
	
	//QueryHelper helper = null;
	LogDataHelper helper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    //helper = new QueryHelper(this);

	    helper = new LogDataHelper(this);
		myDbHelper = new DataBaseHelper(this);
			    
	    //buttons
	    Button scan=(Button)findViewById(R.id.scan);
	    scan.setOnClickListener(onScan);
	    
	    Button database=(Button)findViewById(R.id.database);
	    database.setOnClickListener(onDatabase);
	    
	    Button databaseOpen=(Button)findViewById(R.id.databaseOpen);
	    databaseOpen.setOnClickListener(onDatabaseOpen);
	    
	    Button querylist=(Button)findViewById(R.id.querycheck);
	    querylist.setOnClickListener(onQueryList);
	    
	    Button clear=(Button)findViewById(R.id.clear);
	    clear.setOnClickListener(onClear);
	    	    
	    Button check=(Button)findViewById(R.id.checkExportData);
	    check.setOnClickListener(onCheck);
	    	    
	}
	
	
	/*
	 * Regular stuff from here on
	 */
	
	//Scan Button
	public Button.OnClickListener onScan = new Button.OnClickListener() {
	    public void onClick(View v) {
	        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
	        intent.setPackage("com.google.zxing.client.android");
	        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	        startActivityForResult(intent, 0);
	    }
	};
	
	
	//Clear Button
	public Button.OnClickListener onClear = new Button.OnClickListener() {
	    public void onClick(View v) {
	        
	    	PrintWriter writer;
	    	
			try {
				writer = new PrintWriter(file);
				writer.print("");
		    	writer.close();

			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	    		    	
	    	String clearAlert = cl.clear;
	        Toast clear = Toast.makeText(getBaseContext(), clearAlert, Toast.LENGTH_LONG);
	        clear.show();
	        
	        cl.clearList();
	    }
	};
	
	//Check Export Data - THIS ONE SHOWS THE LOG IN LOG OUT DATA~!
	public Button.OnClickListener onCheck = new Button.OnClickListener(){
		public void onClick(View v){
			
			File sdcard = Environment.getExternalStorageDirectory();
			File file = new File(sdcard,"state.txt");

			//Read text from file
			StringBuilder text = new StringBuilder();
			
			if(file.exists()){
				Toast msg = Toast.makeText(getBaseContext(),"File Exists!", Toast.LENGTH_SHORT);
				msg.show();
			}
			else{
				Toast msg = Toast.makeText(getBaseContext(),"File Doesn't Exist!", Toast.LENGTH_SHORT);
				msg.show();
			}


			try {
			    BufferedReader br = new BufferedReader(new FileReader(file));
			    String line;

			    while ((line = br.readLine()) != null) {
			        text.append(line);
			        text.append('\n');
			    }
			}
			catch (IOException e) {
			    //You'll need to add proper error handling here
			}

			//Find the view by its id
			TextView tv = (TextView)findViewById(R.id.text_view);

			//Set the text
			tv.setText(text);
			tv.setMovementMethod(new ScrollingMovementMethod());
		
		
		}
	};
	
	//Data list version
	public Button.OnClickListener onQueryList = new Button.OnClickListener(){
		public void onClick(View v){
			String commandList = cl.getQueryList(); 
			Toast command = Toast.makeText(getBaseContext(), commandList, Toast.LENGTH_SHORT);
			command.show();
			Toast.makeText(getBaseContext(), commandList, Toast.LENGTH_LONG);
		}
	};

	
	//Open Database!
	public Button.OnClickListener onDatabase = new Button.OnClickListener(){
		
		public void onClick(View v){
			try {
				myDbHelper.createDataBase();

				Toast msg = Toast.makeText(getBaseContext(),"Database Exists!", Toast.LENGTH_SHORT);
				msg.show();
				
			} 
			catch (IOException ioe) {
				throw new Error("Unable to create database");
			}
	  
	 	}

	};
	
	public Button.OnClickListener onDatabaseOpen = new Button.OnClickListener(){
		
		public void onClick(View v){
		
				try {
					myDbHelper.openDataBase();
					Toast msg = Toast.makeText(getBaseContext(),"Database Opened!", Toast.LENGTH_SHORT);
					msg.show();

					
				} catch (java.sql.SQLException e) {
					e.printStackTrace();
				}
				
				Intent intent = new Intent(QRCodeScanner.this, LogAdapter.class);
				startActivity(intent);
			
			
	 	}

	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	            String contents = data.getStringExtra("SCAN_RESULT");
	            
	            // Handle successful scan
	            
	            /*
	             * This is important because the files in the database are formatted in /images/BuildingClassroom
	             * This means that there's no whitespace in between and the query would fail 
	             * What we want to happen is that the QR Code is scanned and instead of basing the dialogue box on the
	             * 		QR Code scanned, we will look for the image in the database. This way, we know if people are trying
	             * 		to cheat or something. 
	             */
	            //newString = contents.replaceAll("TEL:", "");
	            
	            if(contents.contains("TEL:") == false){
	            	newString = "random";
	            	invalidQuery();  
	            }
	            else{
	            	newString = contents.replaceAll("TEL:", "");
	            }
	            
	            //insert database queries here lawl
	            // Since the database was created properly, time to look for the Building Color and Room  
	            // of the room that the dude logged in/out of.
	            // since all the numbers correspond to a room somewhere, it's just simple SQL Query
	            // if the data is fetched properly, all that's left to do is to assign it properly to the string in the alertDialogue
	           	            
	            final String queryResult = search(myDbHelper);
	            
	            //If QR Code is Legit and working, do the rest of awesome stuff.
	            if(queryResult != null){
	            	
	            	final String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
	            	
	            	// Put stuff here, mainly this creates the dialogue box interaction
	            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	                
	            		public void onClick(DialogInterface dialog, int which) {
	            			String inputString = null;
	            			String log = null;
	            			
	            			
	            			//timestamp stuff
	            			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
	            			String date = dateFormat.format(new Date());

	            			Calendar c = Calendar.getInstance(); 
	            			int second = c.get(Calendar.SECOND);
	            			int minute = c.get(Calendar.MINUTE);
	            			int hour = c.get(Calendar.HOUR);
	                	
	            			String timestamp = date + " " + hour + ":" + minute + ":" + second; 
	                	
	            			switch (which){
		            			case DialogInterface.BUTTON_POSITIVE:
		            				inputString = "Log In" + " " + queryResult;
		            				log = "Log In";
		            				cl.setQueryList(inputString); 
		            				break;
	
		            			case DialogInterface.BUTTON_NEGATIVE:
		            				inputString = "Log Out" + " " + queryResult;
		            				log = "Log Out";
		            				cl.setQueryList(inputString);
			                        break;
	            			}
	                    
	            			try {
	            				
	        		            //camera stuff
	        		            Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	        		            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

	        		            //folder stuff
	        		            File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
	        		            imagesFolder.mkdirs();
	        		            
	        		            filePath = "/MyImages/QR_" + timeStamp + ".png" ;
	        		            File image = new File(imagesFolder, "QR_" + timeStamp + ".png");
	        		            Uri uriSavedImage = Uri.fromFile(image);
	        		            
	        		            imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
	        		            startActivityForResult(imageIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);		            

	        		            //save the data to the textfile
		                		FileOutputStream os = new FileOutputStream(file, true); 
		       			     	OutputStreamWriter out = new OutputStreamWriter(os);
		       			     	out.write(android_id + " " + inputString + " " +  timestamp + " " + filePath + "\n");
		       			     		       			     	
		       			     	Toast msg = Toast.makeText(getBaseContext(),"Data Written!", Toast.LENGTH_SHORT);
		       			     	msg.show();
		       			     	
		       			     	//save the data by inserting them to the database
		       			     	helper.insert(android_id, log, queryResult, timestamp, image.toString());
		       			     
		       			     	out.close();
		                	}
		                	catch (IOException e) {
		       		        // Unable to create file, likely because external storage is
		       		        // not currently mounted.
		       				
		                		Toast msg = Toast.makeText(getBaseContext(),"Failed to write!", Toast.LENGTH_LONG);
		                		msg.show();
		       				
		                		Log.w("ExternalStorage", "Error writing " + file, e);
		                	}	
	                    
	            			cl.setQueryList("\n");
	            		}
	            	};
	            

		            //this one over here creates an instance of that dialogue box and displays it
		            AlertDialog.Builder builder = new AlertDialog.Builder(this);
		            
		            builder.setMessage("Welcome to " + queryResult + "!")
		            	.setPositiveButton("Log In!", dialogClickListener)
		                .setNegativeButton("Log Out!", dialogClickListener)
		                .show();
		            

		            
		        }
	            //Just in case wrong QR Code is scanned
	            else{	            	
	            	invalidQuery();
	            }
	        }
	        
	        else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	    }
	    
	    	    
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	//actually nothing needs to be done
	        } 
	        else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } 
	        else {
	            // Image capture failed, advise user
	        }
	    }
	    
	}
	
	
	public String search(DataBaseHelper myDB){
		
		String data = null;
		Cursor cursor = null;
		SQLiteDatabase db = myDB.getReadableDatabase();
		
		try{
			cursor = db.rawQuery("SELECT BuildingColor, Room FROM LSBuildingsDB WHERE _id =" + newString, null);
		}
		catch(Exception e){
			//Nothing to do here. Just so the app doesn't crash like a fucking noob every time I scan a wrong QR Code.
			//All I need to do is to set data to null (done above) and let the rest of the code do the awesome work!
		}
		
		if( cursor != null ){
			cursor.moveToFirst();
			
			data = cursor.getString(cursor.getColumnIndexOrThrow("BuildingColor")) + " " +
		  			cursor.getString(cursor.getColumnIndex("Room"));   
			cursor.close();
		}

        return data;

	}
	

	public void invalidQuery(){
		Toast msg = Toast.makeText(getBaseContext(), "Invalid QR Code Scanned!", Toast.LENGTH_LONG);
	    msg.show();
	    
	}

	
	@Override
	public void onDestroy() { 
		super.onDestroy();
		helper.close(); 
	}
	
	
}