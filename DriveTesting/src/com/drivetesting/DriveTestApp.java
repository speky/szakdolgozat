package com.drivetesting;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener {

	static final String TAG = "DriveTesting";

	private DataStorage dataStorage;
	private SharedPreferences prefs;	
	private String serverIp;	

	private boolean isGpsServiceRun = false;
	private int activeGpsActivity = 0;
	private Context context = null;

	private ReportTask task = new ReportTask(); 
	@Override
	public void onCreate() {
		super.onCreate();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		serverIp= prefs.getString("serverIp", "0.0.0.0");

		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");

		// start location service 
		context = getApplicationContext();
		Intent service = new Intent(context, GPSService.class);
		startService(service);

		//Declare the timer
		Timer t = new Timer();
		//Set the schedule function and rate
		t.scheduleAtFixedRate(
				task,
				//Set how long before to start calling the TimerTask (in milliseconds)
				10,
				//Set the amount of time between each execution (in milliseconds)
				1000);


	}

	public void setGpsService(boolean isRun) {
		isGpsServiceRun = true;
	}
	public void activeGpsActivity(){
		++activeGpsActivity;
	}

	public void deactiveGspActivity() {
		--activeGpsActivity;
	}

	public String getServerIp() {
		return serverIp;
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

	class ReportTask extends TimerTask {
		public void run() {

			System.out.println("timer update");
		}
	}
}
