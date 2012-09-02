package com.drivetesting;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataStorage {
	
	static final  String TAG = "DataStorage"; 
	
	public static final String DB_NAME  = "position_data_db"; 
	public static final int DB_VERSION = 1;
	public static final String DB_TABLE  = "position";
	public static final String C_ID = "id";
	public static final String C_POSITION = "positions";
	
	Context context;
	DbHelper dbHelper;
	SQLiteDatabase db;
	
	public DataStorage(Context context){
		this.context = context;
		dbHelper = new DbHelper();
	}
	
	public void insert (String pos ){
		ContentValues values = new ContentValues();		
		values.put(C_ID, 2);
		values.put(C_POSITION, pos);
		
		db = dbHelper.getWritableDatabase();
		db.insert(DB_TABLE, null, values);
	}
	
	public void querry ( ){
		db = dbHelper.getWritableDatabase();
		
	}
	
	
	class DbHelper extends SQLiteOpenHelper{

		public DbHelper() {
			super(context, DB_NAME, null, DB_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String.format("create table %s (%s int primary key, %s varchar(100))", DB_TABLE , C_ID, C_POSITION );
			Log.d(TAG, "onCreate Sql:"+sql);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Update from "+ oldVersion+" version to "+  newVersion);
			db.execSQL("drop if exist "+ DB_TABLE);
			onCreate(db);
		}
		
	}
}
