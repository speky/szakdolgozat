package com.drivetesting;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener {

	static final String TAG = "DriveTesting";

	DataStorage dataStorage;
	SharedPreferences prefs;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");
	}


	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences pref,	String key) {
		this.prefs = pref;
		Log.d(TAG, "On Change preferences: "+ key);
	}

}
