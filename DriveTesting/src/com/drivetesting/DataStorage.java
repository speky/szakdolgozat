package com.drivetesting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
	public static final String LEVEL = "signal_level";
	public static final String UPSPEED = "up_speed";
	public static final String DOWNSPEED = "down_speed";
	public static final String JITTER = "jitter";
	public static final String PACKETLOST = "lost_packet";
	public static final String SUMPACKET= "sum_packet";
	public static final String MCC = "mcc";
	public static final String MNC = "mnc";
	public static final String LAC = "lac";
	public static final String CID = "cid";
	public static final String NETWORKTYPE = "network_type";
	public static final String RATE = "rate";

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

	public void deleteAll() {
		db.delete(DB_TABLE, null, null);	
	}
	
	private String currentTime(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return df.format(c.getTime());
	}

	public void insert(long testId, String testName, double lat, double lon, double signalStrength, int signalLevel,  double up, 
			double down, double jitter, int lost, int sum,	 int mcc, int mnc, int lac, int cid, int rate, String networkType) {		
		String date = currentTime();
		ContentValues values = new ContentValues();
		values.put(TESTID,  testId);
		values.put(TESTNAME,  testName);
		values.put(TIME,  date);
		values.put(LAT, lat);
		values.put(LON, lon);
		values.put(SIGNAL, signalStrength);
		values.put(LEVEL, signalLevel);
		values.put(UPSPEED, up);
		values.put(DOWNSPEED, down);
		values.put(JITTER, jitter);
		values.put(PACKETLOST, lost);
		values.put(SUMPACKET, sum);
		values.put(MCC, mcc);
		values.put(MNC, mnc);
		values.put(LAC, lac);
		values.put(CID, cid);
		values.put(RATE, rate);
		values.put(NETWORKTYPE, networkType);

		db.insert(DB_TABLE, null, values);
	}

	private DbData cursorToData(Cursor cursor) {
		DbData row = new DbData();	
		row.id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
		row.testId= cursor.getLong(cursor.getColumnIndexOrThrow(TESTID));
		row.testName= cursor.getString(cursor.getColumnIndexOrThrow(TESTNAME));
		row.time = cursor.getString(cursor.getColumnIndexOrThrow(TIME));
		row.signalStrength = cursor.getDouble(cursor.getColumnIndexOrThrow(SIGNAL));
		row.signalLevel = cursor.getInt(cursor.getColumnIndexOrThrow(LEVEL));
		row.lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LAT));
		row.lon = cursor.getDouble(cursor.getColumnIndexOrThrow(LON));
		row.up = cursor.getDouble(cursor.getColumnIndexOrThrow(UPSPEED));
		row.down = cursor.getDouble(cursor.getColumnIndexOrThrow(DOWNSPEED));
		row.jitter = cursor.getInt(cursor.getColumnIndexOrThrow(JITTER));
		row.lost = cursor.getInt(cursor.getColumnIndexOrThrow(PACKETLOST));
		row.sum = cursor.getInt(cursor.getColumnIndexOrThrow(SUMPACKET));
		row.mcc = cursor.getInt(cursor.getColumnIndexOrThrow(MCC));
		row.mnc = cursor.getInt(cursor.getColumnIndexOrThrow(MNC));
		row.lac = cursor.getInt(cursor.getColumnIndexOrThrow(LAC));
		row.cid = cursor.getInt(cursor.getColumnIndexOrThrow(CID));
		row.rate = cursor.getInt(cursor.getColumnIndexOrThrow(RATE));
		row.networkType = cursor.getString(cursor.getColumnIndexOrThrow(NETWORKTYPE));
		return row;
	}
	
	public String getColunNames() {
		StringBuilder columns = new StringBuilder();
		columns.append(ID+",");
		columns.append(TESTID +",");
		columns.append(TESTNAME+",");
		columns.append(TIME +",");
		columns.append(LAT+",");
		columns.append(LON+",");
		columns.append(SIGNAL+",");
		columns.append(LEVEL+",");
		columns.append(UPSPEED+",");
		columns.append(DOWNSPEED+",");
		columns.append(JITTER+",");
		columns.append(PACKETLOST+",");
		columns.append(SUMPACKET+",");
		columns.append(MCC+",");
		columns.append(MNC+",");
		columns.append(LAC+",");
		columns.append(CID+",");
		columns.append(RATE+",");
		columns.append(NETWORKTYPE);		
		return columns.toString();
	}

	public List<DbData> queryAll() {
		List<DbData> dataList = new ArrayList<DbData>();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ DB_TABLE, null);
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
	
	public List<String> queryTestIds() {
		List<String> idList = new ArrayList<String>();		
		Cursor cursor = db.rawQuery("SELECT DISTINCT " + TESTID +" FROM "+ DB_TABLE, null);
		if (cursor != null) {
			cursor.moveToFirst();	
			while (cursor.isAfterLast() == false) {
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(TESTID));
				idList.add(Long.toString(id));
				cursor.moveToNext();
			}
			cursor.close();
		}		
		return idList;
	}
	
	public List<String> queryTestNames() {
		List<String> idList = new ArrayList<String>();		
		Cursor cursor = db.rawQuery("SELECT DISTINCT " + TESTNAME +" FROM "+ DB_TABLE, null);
		if (cursor != null) {
			cursor.moveToFirst();	
			while (cursor.isAfterLast() == false) {				
				idList.add(cursor.getString(cursor.getColumnIndexOrThrow(TESTNAME)));
				cursor.moveToNext();
			}
			cursor.close();
		}		
		return idList;
	}

	public DbData queryLastInsertedRow() {
		DbData data= null;		
		Cursor cursor = db.rawQuery("SELECT * FROM "+ DB_TABLE +" ORDER BY "+ ID +" DESC LIMIT 1", null);
	
		if (cursor != null) {
			cursor.moveToFirst();
			data = cursorToData(cursor);				
			cursor.close();
		}
		return data;		
	}
	
	public List<DbData> querySpecifiedTest(String testId) {
		List<DbData> dataList = new ArrayList<DbData>();

		String[] whereArgs = new String[] {
				testId
		};
		Cursor cursor = db.rawQuery("SELECT * FROM  "+ DB_TABLE + " WHERE " + TESTID +"  = ?", whereArgs);

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

	public List<DbData> querySpecifiedTestByName(String testName) {
		List<DbData> dataList = new ArrayList<DbData>();

		String[] whereArgs = new String[] {
				testName
		};
		Cursor cursor = db.rawQuery("SELECT * FROM "+ DB_TABLE + " WHERE " + TESTNAME +"  = ?", whereArgs);

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

	class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = 
					String.format("create table %s (%s integer primary key autoincrement, %s integer, %s varchar(100), %s varchar(100), %s varchar(15), " +
							"%s varchar(15),  %s varchar(15),  %s varchar(15),%s varchar(15), %s varchar(15), %s varchar(15), %s integer, %s integer, %s integer, %s integer, %s integer, %s integer, %s integer, %s varchar(30))", 
							DB_TABLE , ID, TESTID, TESTNAME, TIME,  LAT, LON, SIGNAL, LEVEL, UPSPEED, DOWNSPEED, JITTER, PACKETLOST, SUMPACKET, 
							MCC, MNC, LAC, CID, RATE, NETWORKTYPE);

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
