package com.drivetesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
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

import com.drivetesting.Observers.LocationObserver;
import com.drivetesting.Observers.PhoneStateObserver;
import com.drivetesting.Observers.TestObserver;
import com.drivetesting.Services.GPSService;
import com.drivetesting.Services.HttpService;
import com.drivetesting.Services.PhoneStateListenerService;
import com.drivetesting.Subjects.LocationSubject;
import com.drivetesting.Subjects.PhoneStateSubject;
import com.drivetesting.Subjects.TestSubject;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener, 
											TestSubject, PhoneStateSubject, LocationSubject{

	static final String TAG = "DriveTesting";

	public static final int UDP = 0;
	public static final int TCP = 1;
	public static final int UPLOAD = 0;
	public static final int DOWNLOAD = 1;
	
	private int MNC = 0;
	private int MCC = 0;
	private int LAC = 0;
	private int CID = 0;
	private double signalStrength = 0.0;
	private long testId = -1;
	private String testName = "-";
	private double DLSpeed = 0.0;
	private double ULSpeed = 0.0;
	
	private DataStorage dataStorage;
	private Timer dataStorageTimer;
	
	private SharedPreferences prefs;	
		
	private boolean networkConnected = false;	
	
	private ReportTask task = new ReportTask();
	private boolean isTestRunning = false;

	private ArrayList<TestObserver> testObservers;
	private ArrayList<PhoneStateObserver> phoneStateObservers;
	private ArrayList<LocationObserver> locationObservers;
	
	private StringBuilder message = new StringBuilder();
	private int action = 0;

	private boolean isGpsServiceRun = false;
	public boolean isGPSEnabled = false;
	private Location location = null;
	
	private void tokenizeMessage(String message) {
		String[] tokens = message.split(" ");
		for (int i = 0; i < tokens.length; ++i) {
			String token = tokens[i];
			if (token.equals("DL")) {
				DLSpeed = Double.parseDouble(tokens[++i]); 
			}
			if (token.equals("UL")) {
				ULSpeed = Double.parseDouble(tokens[++i]); 
			}
		}
	}
	
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
            	String data = msg.getData().getString("packet");
            	message.append(data +"\n");
            	tokenizeMessage(data);
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

		testObservers = new ArrayList<TestObserver>();
		phoneStateObservers = new ArrayList<PhoneStateObserver>();
		locationObservers = new ArrayList<LocationObserver>();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");
		
		// start phone state service
		startService(new Intent(getApplicationContext(), PhoneStateListenerService.class));
		startTimer();
	}

	class ReportTask extends TimerTask {
		public void run() {
			if (isTestRunning) {
				String lon = "-";
				String lat = "-"; 
				if (location != null ) {
					Double.toString(location.getLatitude());
					Double.toString(location.getLongitude());
				}				 
				dataStorage.insert(testId, 
													testName, 
													lat, 
													lon,
													Double.toString(signalStrength),													 
													Double.toString(ULSpeed),
													Double.toString(DLSpeed),
													MCC,
													MNC,
													LAC,
													CID);
			}
		}
	}
	
	public void startGPSService() {
		startService(new Intent(getApplicationContext(), GPSService.class));
	}
	
	private void startTimer() {
		//Declare the timer
		dataStorageTimer = new Timer();
		//Set the schedule function and rate
		dataStorageTimer.scheduleAtFixedRate(
				task,
				//Set how long before to start calling the TimerTask (in milliseconds)
				10,
				//Set the amount of time between each execution (in milliseconds)
				1000);
	}
	
	public boolean startHttpClientService(int direction, int type) {		
		dataStorage.open();
		Intent httpIntent = new Intent(this, HttpService.class);
		if (handler != null) {
			httpIntent.putExtra("handler", new Messenger(handler));						
		}
		dataStorage.insert(3, "testName", "0", "0", "0", "0", "0", 2, 2, 2, 2);
		dataStorage.insert(1, "testName", "0", "0", "0", "0", "0", 2, 2, 2, 2);
		testId = dataStorage.getMaxTestId();
		testName = prefs.getString("testName", "-");		
		httpIntent.putExtra("serverIp", prefs.getString("serverIp", "0.0.0.0"));
		httpIntent.putExtra("direction", direction);
		httpIntent.putExtra("type", type);
				
		httpIntent.putExtra("bufferSize", prefs.getString("bufferSize", "100"));
		httpIntent.putExtra("reportPeriod", prefs.getString("reportPeriod", "100"));
		isTestRunning = true;
		startService(httpIntent);
		return true;
	}

	public void  stopGPSService() {
		Intent intent = new Intent(this, GPSService.class);		
		stopService(intent);
	}
	
	public void stopHttpClientService() {
		Intent httpIntent = new Intent(this, HttpService.class);		
		stopService(httpIntent);
		isTestRunning = false;
		clearTestMessage();
		dataStorageTimer = null;		
//		dataStorage.close();
	}

	public List<DbData> querryTestData(int testId) {
		if (testId < 0) {
			return dataStorage.querryAll();
		}else {
			return dataStorage.querrySpecifiedTest(String.valueOf(testId));
		}
		
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
		signalStrength = Double.parseDouble(value);
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
		MCC = Integer.parseInt(mcc);
		MNC = Integer.parseInt(mnc);
		LAC = Integer.parseInt(lac);
		CID = Integer.parseInt(cid);
		notifyCellLocationChange(cid, lac, mcc, mnc);
	}
	
	public void updateLocation(Location loc) {
		location = loc;
		notifyLocationObservers();
	}
	
	public boolean isTestRunning() {
		return isTestRunning ;
	}
	
	public void setGpsService(boolean isRun) {
		isGpsServiceRun = isRun;
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
		} else if (key.equals("testName")){
			testName = prefs.getString("testName", "-");
			Log.d(TAG, "Test's name has changed");
		}
	}
	
	// test data observer methods
	@Override
	public void registerObserver(TestObserver testObserver) {
		testObservers.add(testObserver);
	}
	@Override
	public void removeObserver(TestObserver testObserver) {
		int index = testObservers.indexOf(testObserver);
		if (index > 0 ) {
			testObservers.remove(index);
		}
	}
	@Override
	public void notifyObservers() {
		for (TestObserver testObserver :testObservers) {
			if (testObserver != null) {
				testObserver.update(action, message.toString());
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
	
// handle location observers
	@Override
	public void registerObserver(LocationObserver observer) {
		locationObservers.add(observer);			
	}
	@Override
	public void removeObserver(LocationObserver observer) {
		int index = locationObservers.indexOf(observer);
		if (index > 0 ) {
			locationObservers.remove(index);
		}		
	}
	@Override
	public void notifyLocationObservers() {
		for (LocationObserver locationObserver :locationObservers) {
			if (locationObserver != null && location != null) {
				locationObserver.update(location.getLatitude(), location.getLongitude());
			}
        }
	}
}
