package com.drivetesting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataStorage {
	
	static final  String TAG = "DataStorage"; 
	
	public static final String DB_NAME  = "drive_test_db"; 
	public static final int DB_VERSION = 1;
	public static final String DB_TABLE  = "test_data";
	public static final String C_ID = "id";
	public static final String C_TIME = "time";
	public static final String C_POSITION = "positions";
	public static final String C_SIGNAL = "rssi";
	
	private Context context;
	private DbHelper dbHelper;
	private SQLiteDatabase db;
	private int size = 0;
	
	public DataStorage(Context context){
		this.context = context;
		dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
		size = 0;
	}
	
	public void insert (String pos, String date, String signalStrength){
		ContentValues values = new ContentValues();
		size++;
		values.put(C_ID, size);
		values.put(C_TIME,  date);
		values.put(C_POSITION, pos);
		values.put(C_SIGNAL, signalStrength);
				
		db.insert(DB_TABLE, null, values);
	}
	
	public String[] querry ( ){
		db = dbHelper.getWritableDatabase();				
		Cursor query = db.query(DB_TABLE, null, null, null, null, null, null);//C_TIME +" DESC");
		List<DbData> dataList = new ArrayList<DbData>();
        while (query.moveToNext()) {
            DbData line= new DbData();
            line.id = query.getString(query.getColumnIndexOrThrow(C_ID));
            dataList.add(line);
        }
        query.close();
        return dataList.toArray(new String[0]);
		
	}	
	
	class DbData{		
		public String id;
		public String position;
		public String time;
		public String signalStrength;
	}
	
	class DbHelper extends SQLiteOpenHelper{

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String.format("create table %s (%s int primary key, %s varchar(100), %s varchar(100), %s varchar(100))", DB_TABLE , C_ID, C_TIME, C_POSITION , C_SIGNAL);
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
