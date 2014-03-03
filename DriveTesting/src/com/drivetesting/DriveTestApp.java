package com.drivetesting;

import http.testhandler.TCPReport;
import http.testhandler.UDPReport;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import com.drivetesting.observers.PhoneStateObserver;
import com.drivetesting.observers.TestObserver;
import com.drivetesting.services.GPSService;
import com.drivetesting.services.HttpService;
import com.drivetesting.services.PhoneStateListenerService;
import com.drivetesting.subjects.PhoneStateSubject;
import com.drivetesting.subjects.TestSubject;

public class DriveTestApp extends Application implements TestSubject, PhoneStateSubject {

	static final String TAG = "DriveTesting";

	public static final int UDP = 0;
	public static final int TCP = 1;
	public static final int UPLOAD = 0;
	public static final int DOWNLOAD = 1;
	
	public static final int ACTION_END = 0;
	public static final int ACTION_REPORT = 1;
	
	public static final int SIGNAL_UNKOWN = 0;
	public static final int SIGNAL_WEAK = 1;
	public static final int SIGNAL_MODERATE = 2;
	public static final int SIGNAL_GOOD = 3;
	public static final int SIGNAL_GREAT = 4;

	public static final int GSM_SIGNAL_STRENGTH_GREAT = 12;
	public static final int GSM_SIGNAL_STRENGTH_GOOD = 8;
	public static final int GSM_SIGNAL_STRENGTH_MODERATE = 5;
    
	private DataStorage dataStorage;
	private SharedPreferences prefs;

	private int MNC = 0;
	private int MCC = 0;
	private int LAC = 0;
	private int CID = 0;
	private double signalStrength = 0.0;
	private int signalLevel = SIGNAL_UNKOWN;
	private int testId = 0;
	private String testName = "";
	private int rateType = 0;
	private boolean networkConnected = false;
	private String networkType = ""; 
	private boolean isTestRunning = false;

	private ArrayList<TestObserver> testObservers;
	private ArrayList<PhoneStateObserver> phoneStateObservers;

	private StringBuilder message = new StringBuilder();
	private int action = ACTION_END;

	public boolean isGpsServiceRun = false;
	public boolean isGPSEnabled = false;
	private Location location = null;

	private String getTimeStamp() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);		
		return dateFormat.format(date);
	}
	
	private Handler handler = new Handler() { 
		@Override 
		public void handleMessage(Message msg) {
			if ( msg.getData().containsKey("error")) {        		
				stopHttpClientService();
				message.append(msg.getData().getString("error") +"\n");
				action = ACTION_END;
				Log.d(TAG, message.toString());
			}
			
			if ( msg.getData().containsKey("TCP")) {            	
				String data = msg.getData().getString("TCP");
				message.insert(0, getTimeStamp()+": "+data +"\n");
				action = ACTION_REPORT;
				TCPReport report = new TCPReport();
				report.parseReport(data);				
				Log.d(TAG, "report data: " + message.toString());
				storeTCPReportItem(report.dlSpeed, report.ulSpeed);

			} else if ( msg.getData().containsKey("UDP")) {            	
				String data = msg.getData().getString("UDP");
				message.insert(0, getTimeStamp()+": "+data +"\n");
				action = ACTION_REPORT;
				
				UDPReport report = new UDPReport();
				report.parseReport(data);				
				Log.d(TAG, "get data" + message.toString());
				storeUDPReportItem(report.dlSpeed, report.ulSpeed, report.jitter, report.lostDatagram, report.sumDatagram);
			}

			if ( msg.getData().containsKey("end")) {
				message.append(msg.getData().getString("end")+"\n");      	
				action = ACTION_END;
				Log.d(TAG, message.toString());
			}
			notifyReportObservers();
			super.handleMessage(msg); 
		} 
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "App created");
		testObservers = new ArrayList<TestObserver>();
		phoneStateObservers = new ArrayList<PhoneStateObserver>();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		
		dataStorage = new DataStorage(this);
		dataStorage.open();	

	/*	dataStorage.deleteAll();
		dataStorage.insert(3, "testName3", 12.0, 3.0, 0.0, 0, 0.0, 0.0, 0.0, 0, 0, 2, 2, 2, 2, 1, "networkType");
	
		dataStorage.insert(1, "testName1", 47.497147, 19.070567, 1.1, 1, 1.1, 0.0, 0.0, 0, 0,2, 2, 2, 2, 1, "UTMS");
		dataStorage.insert(1, "testName1", 47.497219, 19.069383, 5.2, 2, 2.2, 0.0, 0.0, 0, 0,2, 2, 2, 2, 1, "GSM");
		dataStorage.insert(1, "testName1", 47.497994, 19.068972, 8.3, 3, 9.3, 0.0, 0.0, 0, 0,2, 2, 2, 2, 1, "GSM");
		//dataStorage.insert(1, "testName1", 47.495769, 19.070244, 13.4, 4, 14.2, 0.0, 0.0, 0, 0,2, 2, 2, 2, 1, "EDGE");
	*/	 
		// start phone state service
		startService(new Intent(getApplicationContext(), PhoneStateListenerService.class));	
	}

	public void fakeinsert() {
		dataStorage.insert(1, "testName1", 47.495769, 19.070244, 13.4, 4, 14.2, 0.0, 0.0, 0, 0,2, 2, 2, 2, 1, "EDGE");
	}
	
	public SharedPreferences getSharedPreference() {
		return prefs;
	}
	
	public void setSharedPreference(SharedPreferences pref) {
		prefs = pref;
	}
	
	private void storeTCPReportItem(final double dlSpeed, final double ulSpeed) {
		double lat = 0;
		double lon = 0;
		if (location != null ) {
			lon = location.getLongitude();
			lat = location.getLatitude();
		}				 
		dataStorage.insert(testId, 
				testName, 
				lat, 
				lon,
				signalStrength,
				signalLevel,
				ulSpeed,
				dlSpeed,
				0.0,
				0,
				0,
				MCC,
				MNC,
				LAC,
				CID,
				rateType, 
				networkType);			
	}

	private void storeUDPReportItem(final double dlSpeed, final double ulSpeed, final double jitter, final int lost, final int sum) {
		double lat = 0;
		double lon = 0;
		if (location != null ) {
			lon = location.getLongitude();
			lat = location.getLatitude();
		}				 
		dataStorage.insert(testId, 
				testName, 
				lat, 
				lon,
				signalStrength,
				signalLevel,
				ulSpeed,
				dlSpeed,
				jitter,
				lost,
				sum,
				MCC,
				MNC,
				LAC,
				CID, 
				rateType, 
				networkType );
	}


	public void startGPSService() {
		startService(new Intent(this, GPSService.class));
	}

	public void  stopGPSService() {
		Intent intent = new Intent(this, GPSService.class);		
		stopService(intent);
	}
	
	public String getTestName() {
		return testName;
	}
	
	public long getTestId() {
		return testId;
	}
	
	public boolean startHttpClientService(int direction, int type) {
		clearTestMessage();
		++testId;
		Intent httpIntent = new Intent(this, HttpService.class);
		if (handler != null) {
			httpIntent.putExtra("handler", new Messenger(handler));						
		}

		testName = prefs.getString("testName", "");		
		httpIntent.putExtra("serverIp", prefs.getString("serverIp", "0.0.0.0"));
		httpIntent.putExtra("direction", direction);
		httpIntent.putExtra("type", type);				
		httpIntent.putExtra("bufferSize", prefs.getString("bufferSize", "8000"));
		httpIntent.putExtra("reportPeriod", prefs.getString("reportPeriod", "1000"));
		httpIntent.putExtra("udprate", prefs.getString("udprate", "1024"));
		
		rateType = prefs.getInt("rateType", 1);
		httpIntent.putExtra("rateType", Integer.toString(rateType));

		isTestRunning = true;
		startService(httpIntent);
		return true;
	}
		
	public void stopHttpClientService() {
		Intent httpIntent = new Intent(this, HttpService.class);		
		stopService(httpIntent);
		isTestRunning = false;				
		//		dataStorage.close();
	}

	public DbData queryLastInsertedRow() {
		return dataStorage.queryLastInsertedRow();
	}
	
	public List<DbData> queryTestDataByName(String name) {
		if (name == null  || name.equals("ALL")) {
			return dataStorage.queryAll();
		}else {
			return dataStorage.querySpecifiedTestByName(name);
		}		
	}

	public   List<String> getTestNames() {
		return dataStorage.queryTestNames();
	}
	
	public List<DbData> queryTestData(long id) {
		if (id < 0) {
			return dataStorage.queryAll();
		}else {
			return dataStorage.querySpecifiedTest(String.valueOf(id));
		}		
	}

	public   List<String> getTestIds() {
		return dataStorage.queryTestIds();
	}

	public DataStorage getDataStorage() {
		return dataStorage;
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
		networkType = value;
		notifyNetworkTypeChange(value);
	}		
	public void setSignalStrength(final String value) {
		signalStrength = Double.parseDouble(value);
		notifySignalStrengthChange(value);
	}
	public void setSignalLevel(final int value) {
		signalLevel = value;		
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

	// test data observer methods
	@Override
	public void registerReportObserver(TestObserver testObserver) {
		testObservers.add(testObserver);
	}
	@Override
	public void removeReportObserver(TestObserver testObserver) {
		int index = testObservers.indexOf(testObserver);
		if (index > 0 ) {
			testObservers.remove(index);
		}
	}	
	@Override
	public void notifyReportObservers() {
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

}
