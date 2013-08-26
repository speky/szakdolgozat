package com.drivetesting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class CustomPhoneStateListener extends Service{
	
	private static final  String TAG = "CustomPhonestateListner";
	private static final int EXCELLENT_LEVEL = 75;
	private static final int GOOD_LEVEL = 50;
	private static final int MODERATE_LEVEL = 25;
	private static final int WEAK_LEVEL = 0;
	
	private static final String NO_SIGNAL_STRENGTH = "?";
	
	private TelephonyManager telephonyManager = null;
	private BroadcastReceiver connectivityBroadcastReceiver = null;
	private IntentFilter networkStateChangedFilter;
	private boolean isNetworkConnected = false;

	//dynamically changed variable
	private String signalStrengthString = "99";
	private String cdmaEcio = "-1";
	private String evdoDbm = "-1";
	private String evdoEcio = "-1";
	private String evdoSnr = "-1";
	private String gsmBitErrorRate = "-1"; 
	private String serviceStateString = "";
	private String callState = "";
	private String networkState = "";
	private String dataConnectionState = "";
	private String dataDirection= "";
	private String networkType= "";
	private Context context;
	
	CustomPhoneStateListener(Context context){
		this.context = context;
		networkStateChangedFilter = new IntentFilter();
		networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		connectivityBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {				
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
					setConnectionState(false);
					networkState = "Nincs kapcsolat";
					networkType = "No network";
					if (networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE ||
							networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
						networkType = networkInfo.getSubtypeName();
						Log.d(TAG, "connectivity network_Type: "+networkType);
						if (networkInfo.isConnected()) {
							setConnectionState(true);
							networkState = "Kapcsolódva";
						}
					}
					
				}
			}
		};

		registerReceiver(connectivityBroadcastReceiver, networkStateChangedFilter);

	}
	
	public boolean isInternetConnectionActive() {
		return isNetworkConnected;
	}

	private void setConnectionState(boolean connectionState ) {
		isNetworkConnected = connectionState;
	}

	
	public void startSignalLevelListener() {
		if (telephonyManager == null){  
			telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		}
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | 
				PhoneStateListener.LISTEN_DATA_ACTIVITY |
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |				
				PhoneStateListener.LISTEN_CELL_LOCATION |
				PhoneStateListener.LISTEN_CALL_STATE |				
				PhoneStateListener.LISTEN_SERVICE_STATE;

		telephonyManager.listen(this, events);
				
	}

	public void stopListening(){
		if (telephonyManager == null){  
			telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);	
		}

		if (connectivityBroadcastReceiver != null) {
			unregisterReceiver(connectivityBroadcastReceiver);
		}
	}

	private String getSignalLevelString(int level) {
		String signalLevelString = "Weak";

		if (level > EXCELLENT_LEVEL){
			signalLevelString = "Excellent";
		}
		else if (level > GOOD_LEVEL){
			signalLevelString = "Good";
		}
		else if (level > MODERATE_LEVEL){
			signalLevelString = "Moderate";
		}
		else if (level > WEAK_LEVEL){
			signalLevelString = "Weak";
		}
		return signalLevelString;
	}
	
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength)
	{
		super.onSignalStrengthsChanged(signalStrength);

		switch (telephonyManager.getPhoneType()){
		case TelephonyManager.PHONE_TYPE_CDMA:
			signalStrengthString = String.valueOf(signalStrength.getCdmaDbm()) + context.getString(R.string.unit_dbm); 
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			int rssi = signalStrength.getGsmSignalStrength();
			int rssi_dbm = -113 + 2 *  rssi;
			signalStrengthString = String.valueOf(rssi_dbm) + context .getString(R.string.unit_dbm);
			break;
		case TelephonyManager.PHONE_TYPE_NONE:
		default:
			signalStrengthString = NO_SIGNAL_STRENGTH;
			break;
		}

		cdmaEcio = String.valueOf(signalStrength.getCdmaEcio());
		evdoDbm = String.valueOf(signalStrength.getEvdoDbm())+ context .getString(R.string.unit_dbm);
		evdoEcio = String.valueOf(signalStrength.getEvdoEcio());
		evdoSnr = String.valueOf(signalStrength.getEvdoSnr());
		gsmBitErrorRate = String.valueOf(signalStrength.getGsmBitErrorRate());

		Log.d(TAG, " Signal strength changed: " + signalStrength);
		Toast.makeText(context, "Signal strength changed!  ", Toast.LENGTH_SHORT).show();		
	}

	@Override
	public void onCallStateChanged(final int state, final String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);

		callState = "Ismeretlen";
		switch(state)
		{
		case TelephonyManager.CALL_STATE_IDLE:  
			callState = "Tétlen"; 
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			callState = "Csörög (" + incomingNumber + ")"; 
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:       
			callState = "Hívás közben"; 
			break;
		}
		Log.d(TAG, "phoneState updated - incoming number - " + incomingNumber);
	}

	@Override
	public void onCellLocationChanged(CellLocation location)
	{			
		super.onCellLocationChanged(location);
		String locationString = location.toString();

		Log.d(TAG, "onCellLocationChanged " + locationString);
		//Toast.makeText(getApplicationContext(), "Cell location changed!  ", Toast.LENGTH_SHORT).show();		
	}

	@Override
	public void onDataConnectionStateChanged(int state)
	{
		super.onDataConnectionStateChanged(state);
		Log.d(TAG, "onDataConnectionStateChanged " + state);
		dataConnectionState = getDataState(state);		
	}

	@Override
	public void onDataActivity(int direction)
	{
		super.onDataActivity(direction);
		Log.d(TAG, "onDataActivity " + direction);

		dataDirection = getDataActivity(direction);		
	}

	@Override
	public void onServiceStateChanged(ServiceState serviceState)
	{
		super.onServiceStateChanged(serviceState);

		serviceStateString = "Ismeretlen";
		switch(serviceState.getState())
		{
		case ServiceState.STATE_IN_SERVICE:     
			serviceStateString = "Üzemel"; 
			break;
		case ServiceState.STATE_EMERGENCY_ONLY:         
			serviceStateString = "Csak vészhívás"; 
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			serviceStateString = "Nem mûködik"; 
			break;
		case ServiceState.STATE_POWER_OFF: 
			serviceStateString = "Kikapcsolva"; 
			break;
		default: 
			serviceStateString = "Ismeretlen"; 
			break;
		}
		Log.d(TAG, "onServiceStateChanged " + serviceStateString);	
	}	

	private String getPhoneType(){
		String phoneType = "Ismeretlen";
		switch (telephonyManager.getPhoneType()){
		case TelephonyManager.PHONE_TYPE_NONE : 
			phoneType = "NONE";
			break;
		case TelephonyManager.PHONE_TYPE_GSM : 
			phoneType = "GSM";
			break;
		case TelephonyManager.PHONE_TYPE_CDMA : 
			phoneType = "CDMA";
			break;
		case TelephonyManager.PHONE_TYPE_SIP : 
			phoneType = "SIP";
			break;
		}
		return phoneType;
	}

	private String getSimState(){
		String simStateString = "NA";
		switch (telephonyManager.getSimState()) {
		case TelephonyManager.SIM_STATE_ABSENT:
			simStateString = "Nincs SIM kártya";
			break;
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			simStateString = "Zárolt, hálózati PIN kód szükséges";
			break;
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			simStateString = "Zárolt, felhasználó PIN kódja szükséges";
			break;
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			simStateString = "Zárolt, PUK kód szükséges";
			break;
		case TelephonyManager.SIM_STATE_READY:
			simStateString = "Készen áll";
			break;
		case TelephonyManager.SIM_STATE_UNKNOWN:
			simStateString = "Ismeretlen";
			break;
		}        
		return simStateString;
	}

	private String getLAC(){
		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		String lac = "0";
		if (cellLocation != null){
			lac = String.format("%d", cellLocation.getLac());	
		}
		return lac;
	}

	private String getCID(){
		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		String cid = "0";
		if (cellLocation != null){
			cid = String.format("%d", cellLocation.getCid()& 0xffff);	
		}
		return cid;
	}

	private String getDataActivity(int activityType){
		String dataActivity = "";
		switch (activityType){
		case TelephonyManager.DATA_ACTIVITY_NONE :
			dataActivity = "Nincs"; 
			break;
		case TelephonyManager.DATA_ACTIVITY_IN:
			dataActivity = "Bejövõ adat"; 
			break;
		case TelephonyManager.DATA_ACTIVITY_OUT :
			dataActivity = "Kimenõ adat"; 
			break;
		case TelephonyManager.DATA_ACTIVITY_INOUT: 
			dataActivity = "Kétirányú kapcsolat"; 
			break;
		case TelephonyManager.DATA_ACTIVITY_DORMANT:
			dataActivity = "Sérült kapcsolat"; 
			break;
		}
		return dataActivity;
	}
	private String getDataState(int state){
		String dataState = "";
		switch (state){
		case TelephonyManager.DATA_CONNECTED :
			dataState = "Kapcsolódva"; 
			break;
		case TelephonyManager.DATA_CONNECTING:
			dataState = "Kapcsolódás"; 
			break;
		case TelephonyManager.DATA_DISCONNECTED :
			dataState = "Nincs kapcsolat"; 
			break;
		case TelephonyManager.DATA_SUSPENDED: 
			dataState = "Felfüggesztve"; 
			break;
		}
		return dataState;
	}
	private String getTelephoneNumber(){
		return  telephonyManager.getLine1Number();
	}

	private String getRoaming(){
		String roaming = "Nincs";
		if (telephonyManager.isNetworkRoaming()){
			roaming = "Van";
		}
		return roaming;
	}

	private String getNetworkCountry(){
		return telephonyManager.getNetworkCountryIso().toUpperCase();
	}

	private String getNetworkOperatorName(){
		return telephonyManager.getNetworkOperatorName();
	}

	private String getSubscriberId(){
		return telephonyManager.getSubscriberId();	
	}

	private String getIMEI(){
		return telephonyManager.getDeviceId();	
	}

	private String getSimSerialNumber(){
		return telephonyManager.getSimSerialNumber();
	}

	private String getSoftwareVersion(){
		return telephonyManager.getDeviceSoftwareVersion();
	}

	private String getMCC(){
		String simOperator = telephonyManager.getNetworkOperator();
		if (simOperator != null) {
			return simOperator.substring(0, 3);
		}
		return null;
	}

	private String getMNC(){
		String simOperator = telephonyManager.getNetworkOperator();
		if (simOperator != null) {
			return simOperator.substring(3);
		}
		return null;
	}

	
}

