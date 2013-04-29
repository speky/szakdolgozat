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
	
	public static final String DB_NAME  = "drive_test_db1"; 
	public static final int DB_VERSION = 1;
	public static final String DB_TABLE  = "test_dat";
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
		values.put(C_ID,  ++size);
		values.put(C_TIME,  date);
		values.put(C_POSITION, pos);
		values.put(C_SIGNAL, signalStrength);
						
		db.insert(DB_TABLE, null, values);
			    
        //String sql =   "INSERT INTO "+DB_TABLE+ "("+ C_TIME+", "+C_POSITION+", "+C_SIGNAL+") VALUES("+ date+", "+pos+", "+signalStrength +")" ;       
        //db.execSQL(sql);
	}
	
	public List<DbData> querry ( ){
		db = dbHelper.getWritableDatabase();				
		//Cursor query = db.query(DB_TABLE, null, null, null, null, null, null);//C_TIME +" DESC");
		 Cursor query = db.rawQuery("SELECT * FROM "+DB_TABLE , null);//WHERE TRIM(name) = '"+name.trim()+"
		List<DbData> dataList = new ArrayList<DbData>();
        while (query.moveToNext()) {
            DbData line= new DbData();
            line.id = query.getString(query.getColumnIndexOrThrow(C_ID));
            line.position = query.getString(query.getColumnIndexOrThrow(C_POSITION));
            line.time = query.getString(query.getColumnIndexOrThrow(C_TIME));
            line.signalStrength = query.getString(query.getColumnIndexOrThrow(C_SIGNAL));
            dataList.add(line);
        }
        query.close();        
        return dataList;
		
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
			String sql = String.format("create table %s (%s int primary key, %s varchar(200), %s varchar(100), %s varchar(100))", DB_TABLE , C_ID, C_TIME, C_POSITION , C_SIGNAL);
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
