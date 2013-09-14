package com.drivetesting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataStorage {

	static final  String TAG = "DataStorage"; 

	public static final String DB_NAME  = "drive_test_db_v1"; 
	public static final int DB_VERSION = 1;
	public static final String DB_TABLE  = "test_dat";
	public static final String ID = "id";
	public static final String TESTID = "test_id";
	public static final String TESTNAME = "test_name";
	public static final String TIME = "time";
	public static final String LAT= "lat";
	public static final String LON= "lon";
	public static final String SIGNAL = "signal_strenght";
	public static final String UPSPEED = "up_speed";
	public static final String DOWNSPEED = "down_speed";
	public static final String MCC = "mcc";
	public static final String MNC = "mnc";
	public static final String LAC = "lac";
	public static final String CID = "cid";

	private DbHelper dbHelper;
	private SQLiteDatabase db;

	public DataStorage(Context context){
		dbHelper = new DbHelper(context);		
	}

	public void open() {
		try {
			db = dbHelper.getWritableDatabase();
		}
		catch (SQLException ex) {
			Log.e(TAG, "Error: "+ ex.getMessage());
		}
	}

	public void close() {
		dbHelper.close();
	}

	private String currentTime(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(c.getTime());
	}

	public void insert(long testId, String testName, double lat, double lon, double signalStrength, double up, double down,
							int mcc, int mnc, int lac, int cid) {		
		String date = currentTime();
		ContentValues values = new ContentValues();
		values.put(TESTID,  testId);
		values.put(TESTNAME,  testName);
		values.put(TIME,  date);
		values.put(LAT, lat);
		values.put(LON, lon);
		values.put(SIGNAL, signalStrength);
		values.put(UPSPEED, up);
		values.put(DOWNSPEED, down);
		values.put(MCC, mcc);
		values.put(MNC, mnc);
		values.put(LAC, lac);
		values.put(CID, cid);
		
		db.insert(DB_TABLE, null, values);
	}

	private DbData cursorToData(Cursor cursor) {
		DbData row = new DbData();	
		row.id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
		row.testId= cursor.getLong(cursor.getColumnIndexOrThrow(TESTID));
		row.testName= cursor.getString(cursor.getColumnIndexOrThrow(TESTNAME));
		row.time = cursor.getString(cursor.getColumnIndexOrThrow(TIME));
		row.signalStrength = cursor.getDouble(cursor.getColumnIndexOrThrow(SIGNAL));
		row.lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LAT));
		row.lon = cursor.getDouble(cursor.getColumnIndexOrThrow(LON));
		row.up = cursor.getDouble(cursor.getColumnIndexOrThrow(UPSPEED));
		row.down = cursor.getDouble(cursor.getColumnIndexOrThrow(DOWNSPEED));
		row.mcc = cursor.getInt(cursor.getColumnIndexOrThrow(MCC));
		row.mnc = cursor.getInt(cursor.getColumnIndexOrThrow(MNC));
		row.lac = cursor.getInt(cursor.getColumnIndexOrThrow(LAC));
		row.cid = cursor.getInt(cursor.getColumnIndexOrThrow(CID));
		return row;
	}
	
	public List<DbData> querryAll() {
		List<DbData> dataList = new ArrayList<DbData>();
		//Cursor query = db.query(DB_TABLE, null, null, null, null, null, null);//C_TIME +" DESC");
		Cursor cursor = db.rawQuery("SELECT * FROM "+DB_TABLE , null);
		if (cursor != null) {
			cursor.moveToFirst();		
			while (cursor.isAfterLast() == false) {
				DbData data = cursorToData(cursor);
				dataList.add(data);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return dataList;		
	}
	
	public List<DbData> querrySpecifiedTest(String testId) {
		List<DbData> dataList = new ArrayList<DbData>();

		String[] whereArgs = new String[] {
			    testId
			    //testName
			};
		Cursor cursor = db.rawQuery("SELECT * FROM "+DB_TABLE +
																" WHERE " + TESTID +"  = ?", whereArgs); //OR " + TESTNAME +"= ?
																

		if (cursor != null) {
			cursor.moveToFirst();		
			while (cursor.isAfterLast() == false) {
				DbData data = cursorToData(cursor);
				dataList.add(data);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return dataList;		
	}
	 
	public long getMaxTestId() {				
		/*Cursor cursor = db.rawQuery("SELECT MAX("+TESTID +") FROM "+DB_TABLE, null);
		long id = 1;		
		if (cursor != null) {			
			 id = cursor.getLong(cursor.getColumnIndexOrThrow(TESTID));
			cursor.close();
		}
		return id;*/
		  final SQLiteStatement stmt = db.compileStatement("SELECT MAX("+TESTID +") FROM "+DB_TABLE);

	    return stmt.simpleQueryForLong();
	}
	
	class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = 
					String.format("create table %s (%s integer primary key autoincrement, %s integer, %s varchar(100), %s varchar(100), %s varchar(15), " +
							"%s varchar(15),  %s varchar(15),  %s varchar(15), %s varchar(15), %s integer, %s integer, %s integer, %s integer)", 
							DB_TABLE , ID, TESTID, TESTNAME, TIME,  LAT, LON, SIGNAL, UPSPEED, DOWNSPEED, MCC, MNC, LAC, CID);
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
