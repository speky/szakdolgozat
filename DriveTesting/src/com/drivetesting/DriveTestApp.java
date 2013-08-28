package com.drivetesting;

import java.util.ArrayList;
import java.util.TimerTask;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener, Subject, PhoneStateSubject{

	static final String TAG = "DriveTesting";

	public static final int UDP = 0;
	public static final int TCP = 1;
	public static final int UPLOAD = 0;
	public static final int DOWNLOAD = 1;
	
	private DataStorage dataStorage;
	private SharedPreferences prefs;	
		
	private boolean networkConnected = false;	
	
	private ReportTask task = new ReportTask();
	private boolean isTestRunning = false;

	private ArrayList<Observer> observers;
	private ArrayList<PhoneStateObserver> phoneStateObservers;
	
	private StringBuilder message = new StringBuilder();
	private int action = 0;

	private boolean isGpsServiceRun = false;
	private int activeGpsActivity = 0;
	private Location location = null;
	
	Handler handler = new Handler() 
    { 
        @Override 
        public void handleMessage(Message msg) {
        	if ( msg.getData().containsKey("error")) {        		
        		stopHttpClientService();
        		message.append(msg.getData().getString("error") +"\n");
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
		phoneStateObservers = new ArrayList<PhoneStateObserver>();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");

		// start location service
		startService(new Intent(getApplicationContext(), GPSService.class));
		
		// start location service
		startService(new Intent(getApplicationContext(), PhoneStateListenerService.class));

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
				
		httpIntent.putExtra("bufferSize", prefs.getString("bufferSize", "100"));
		httpIntent.putExtra("reportPeriod", prefs.getString("reportPeriod", "100"));
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
	
	public boolean isInternetConnectionActive() {
		return networkConnected;
	}
	public void setConnectionState(boolean value) {
		networkConnected = value;
	}
	public void setNetworkState(final String value) {
		notifyNetworkStateChange(value);
	}	
	public void setNetworkType(final String value) {
		notifyNetworkTypeChange(value);
	}		
	public void setSignalStrength(final String value) {
		notifySignalStrengthChange(value);
	}
	public void setCdmaEcio(final String value) {
		notifyCdmaEcioChange(value);
	}	
	public void setEvdoDbm(final String value) {
		notifyEvdoDbmChange(value);
	}
	public void setEvdoEcio(final String value) {
		notifyEvdoEcioChange(value);
	}
	public void setEvdoSnr(final String value) {
		notifyEvdoSnrChange(value);
	}
	public void setGsmBitErrorRate(final String value) {
		notifyGsmBitErrorRateChange(value);
	}
	public void setCallState(final String value) {
		notifyCallStateChange(value);
	}
	public void setDataConnectionState(final String value) {
		notifyDataConnectionStateChange(value);
	}
	public void setDataConnectionDirection(final String value) {
		notifyDataDirectionChange(value);
	}
	public void setServiceState(final String value) {
		notifyServiceStateChange(value);
	}
	public void setCellLocation(final String mcc, final String mnc, final String lac, final String cid) {
		notifyCellLocationChange(cid, lac, mcc, mnc);
	}
	
	public void updateLocation(Location loc) {
		location = loc;
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
		if (activeGpsActivity > 0) {
			--activeGpsActivity;
		}
	}

	public String getTestMessage() {
		return message.toString();
	}
	
	public void clearTestMessage() {
		message.delete(0, message.length());
	}
	
	/*private static final Pattern IP_ADDRESS = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
        + "|[1-9][0-9]|[0-9]))");
	*/
	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences pref,	String key) {
		this.prefs = pref;
		Log.d(TAG, "On Change preferences: "+ key);
		if (key.equals("serverIp")) {
			/*Matcher matcher = IP_ADDRESS.matcher(pref.getString("serverIp", null));
			if (matcher.matches()) {
			*/
			    // ip is correct
				Log.d(TAG, "Server IP has changed");
			/*	
			}else {
				Log.d(TAG, "Server IP is wrong");
				final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("Invalid Input");
                builder.setMessage("It is not a valid IP address!");
                builder.setPositiveButton("Ok", null);
                builder.show();
			}*/
			
		}

	}

	class ReportTask extends TimerTask {
		public void run() {

			System.out.println("timer update");
		}
	}
	
	// test data observer methods
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
		for (Observer observer :observers) {
			if (observer != null) {
				observer.update(action, message.toString());
			}
        }
	}

	// phoneStateObserver methods
	public void registerPhoneStateObserver(PhoneStateObserver observer) {
		phoneStateObservers.add(observer);
	}
	public void removePhoneStateObserver(PhoneStateObserver observer){		
		int index = phoneStateObservers.indexOf(observer);
		if (index > 0 ) {
			phoneStateObservers.remove(index);
		}
	}
	public void notifySignalStrengthChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {			
			observer.updateSignalStrength(value);
		}        
	}
	public void notifyCdmaEcioChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateCdmaEcio(value);			
        }
	}
	public void notifyEvdoDbmChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateEvdoDbm(value);			
        }
	}
	public void notifyEvdoEcioChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {			
			observer.updateEvdoEcio(value);			
        }
	}
	public void notifyEvdoSnrChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {			
			observer.updateEvdoSnr(value);
		}
	}
	public void notifyGsmBitErrorRateChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
				observer.updateGsmBitErrorRate(value);
        }
	}
	public void notifyServiceStateChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateServiceState(value);
        }
	}
	public void notifyCallStateChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateCallState(value);
        }
	}
	public void notifyNetworkStateChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateNetworkState(value);
        }
	}
	public void notifyDataConnectionStateChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateDataConnectionState(value);
        }
	}
	public void notifyDataDirectionChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateDataDirection(value);
        }
	}
	public void notifyNetworkTypeChange(String value){
		for (PhoneStateObserver observer :phoneStateObservers) {
			observer.updateNetworkType(value);
        }
	}
	public void notifyCellLocationChange(String mnc, String mcc, String lac, String cid){
		for (PhoneStateObserver observer :phoneStateObservers) {
				observer.updateCellLocation(mnc, mcc, lac, cid);
        }
	}
	
}
