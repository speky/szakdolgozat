package com.drivetesting;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener {

	static final String TAG = "DriveTesting";

	private DataStorage dataStorage;
	private SharedPreferences prefs;	
	private String serverIp;
	
	public String getServerIp() {
		return serverIp;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		serverIp= prefs.getString("serverIp", "0.0.0.0");
		
		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");
	}

	

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences pref,	String key) {
		this.prefs = pref;
		Log.d(TAG, "On Change preferences: "+ key);
        if (key.equals("serverIp")) {
            //Preference connectionPref = findPreference(key);
        	Log.d(TAG, "Server IP changed");
        }

	}

}
