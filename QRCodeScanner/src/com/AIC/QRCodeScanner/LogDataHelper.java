package com.AIC.QRCodeScanner;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.content.ContentValues;

class LogDataHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME="logList.db"; private static final int SCHEMA_VERSION=1;
	
	public LogDataHelper(Context context) {
	    super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) { 
		//db.execSQL("CREATE TABLE restaurants (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, type TEXT, notes TEXT);");
		db.execSQL("CREATE TABLE logData (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
												"name TEXT, " +
												"log TEXT, " +
												"room TEXT, " +
												"time TEXT, " +
												"path TEXT);" );
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		  // no-op, since will not be called until 2nd schema version exists
	}
	
	public void insert(String name, String log, String room, String time, String path) {
		ContentValues cv=new ContentValues();
		cv.put("name", name); 
		cv.put("log", log); 
		cv.put("room", room); 
		cv.put("time", time);
		cv.put("path", path);
		
		getWritableDatabase().insert("logData", "name", cv); 
	}
	
	public Cursor getAll() { 
		return( getReadableDatabase()
				.rawQuery("SELECT _id, name, log, room, time, path FROM logData", null) );
	}
	
	public String getName(Cursor c){
		return(c.getString(1));
	}
	
	public String getLog(Cursor c){
		return(c.getString(2));
	}
	
	public String getRoom(Cursor c){
		return(c.getString(3));
	}
	
	public String getTime(Cursor c){
		return(c.getString(4));
	}
	
	public String getPath(Cursor c){
		return(c.getString(5));
	}
	
}