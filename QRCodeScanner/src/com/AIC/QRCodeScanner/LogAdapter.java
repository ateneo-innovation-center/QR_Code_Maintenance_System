package com.AIC.QRCodeScanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class LogAdapter extends ListActivity { 
	private static final int DIALOG_ID = 100;

	private SQLiteDatabase database;

	private CursorAdapter dataSource;

	private View entryView;

	private EditText firstNameEditor;

	private EditText lastNameEditor;

	private static final String fields[] = { "name", "log", "room", "time", "path", BaseColumns._ID };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LogDataHelper helper = new LogDataHelper(this);
		database = helper.getWritableDatabase();
		
		Cursor data = database.query("logData", fields, null, null, null, null, null);
		
		dataSource = new SimpleCursorAdapter(this, R.layout.row, data, fields,
				new int[] { R.id.ID, R.id.Log, R.id.Room, R.id.timeStamp, R.id.filePath });

		ListView view = getListView();
		view.setHeaderDividersEnabled(true);
		view.addHeaderView(getLayoutInflater().inflate(R.layout.row, null));

		setListAdapter(dataSource);
	}


	
}