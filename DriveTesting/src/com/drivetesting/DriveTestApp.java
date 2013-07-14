package com.drivetesting;

import java.util.ArrayList;
import java.util.TimerTask;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener, Subject{

	static final String TAG = "DriveTesting";

	private DataStorage dataStorage;
	private SharedPreferences prefs;	
	
	private boolean isGpsServiceRun = false;
	private int activeGpsActivity = 0;
	private Context context = null;

	private ReportTask task = new ReportTask();
	private boolean isTestRunning = false;

	private ArrayList<Observer> observers;
	private String message = "";
	private int action = 0;
	
	Handler handler = new Handler() 
    { 
        @Override 
        public void handleMessage(Message msg) { 
        	if ( msg.getData().containsKey("error")) {
        		message = msg.getData().getString("error");        		
            	stopHttpClientService();
            	action = 0;
            	Log.d(TAG, message);
            } 
            
            if ( msg.getData().containsKey("packet")) {
            	message = msg.getData().getString("packet");
            	action = 1;
            	Log.d(TAG, "get data" + message);
            } 
            
            if ( msg.getData().containsKey("end")) {
            	message = msg.getData().getString("end");            	
            	action = 0;
            	Log.d(TAG, message);            	
            }
            notifyObservers();
            super.handleMessage(msg); 
        } 
    };
	
	@Override
	public void onCreate() {
		super.onCreate();

		observers = new ArrayList<Observer>();
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");

		// start location service 
		context = getApplicationContext();
		Intent service = new Intent(context, GPSService.class);
		startService(service);
			
		//Declare the timer
		/*Timer t = new Timer();
		//Set the schedule function and rate
		t.scheduleAtFixedRate(
				task,
				//Set how long before to start calling the TimerTask (in milliseconds)
				10,
				//Set the amount of time between each execution (in milliseconds)
				1000);
		 */

	}

	public boolean startHttpClientService() {		
		Intent httpIntent = new Intent(this, HttpClient.class);
		if (handler != null) {
			httpIntent.putExtra("handler", new Messenger(handler));						
		}
		httpIntent.putExtra("serverIp", prefs.getString("serverIp", "0.0.0.0"));
		isTestRunning = true;
		startService(httpIntent);
		return true;
	}

	public boolean stopHttpClientService() {
		Intent httpIntent = new Intent(this, HttpClient.class);		
		stopService(httpIntent);
		isTestRunning = false;
		return true;
	}
	
	public boolean isTestRunning() {
		return isTestRunning ;
	}
	
	public void setGpsService(boolean isRun) {
		isGpsServiceRun = true;
	}
	
	public void activeGpsActivity() {
		++activeGpsActivity;
	}

	public void deactiveGspActivity() {
		--activeGpsActivity;
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

	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
		int index = observers.indexOf(observer);
		if (index > 0 ) {
			observers.remove(index);
		}
	}

	@Override
	public void notifyObservers() {
		for (int i = 0; i < observers.size(); i++) {
            Observer observer = (Observer)observers.get(i);
            observer.update(action, message);
        }		
	}


}
