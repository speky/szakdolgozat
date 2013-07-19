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
	private StringBuilder message = new StringBuilder();
	private int action = 0;
	
	Handler handler = new Handler() 
    { 
        @Override 
        public void handleMessage(Message msg) {
        	if ( msg.getData().containsKey("error")) {
        		message.append(msg.getData().getString("error")+"\n");        		
            	stopHttpClientService();
            	action = 0;
            	Log.d(TAG, message.toString());
            }
            
            if ( msg.getData().containsKey("packet")) {
            	message.append(msg.getData().getString("packet")+"\n");
            	action = 1;
            	Log.d(TAG, "get data" + message.toString());
            } 
            
            if ( msg.getData().containsKey("end")) {
            	message.append(msg.getData().getString("end")+"\n");      	
            	action = 0;
            	Log.d(TAG, message.toString());
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

	public boolean startHttpClientService(int direction, int type) {		
		Intent httpIntent = new Intent(this, HttpClient.class);
		if (handler != null) {
			httpIntent.putExtra("handler", new Messenger(handler));						
		}
		httpIntent.putExtra("serverIp", prefs.getString("serverIp", "0.0.0.0"));
		httpIntent.putExtra("direction", direction);
		httpIntent.putExtra("type", type);
		isTestRunning = true;
		startService(httpIntent);
		return true;
	}

	public boolean stopHttpClientService() {
		Intent httpIntent = new Intent(this, HttpClient.class);		
		stopService(httpIntent);
		isTestRunning = false;
		clearTestMessage();
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

	public String getTestMessage() {
		return message.toString();
	}
	
	public void clearTestMessage() {
		message.delete(0, message.length());
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
            observer.update(action, message.toString());
        }		
	}


}
