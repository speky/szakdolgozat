package com.drivetesting.services;

import java.util.Locale;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.drivetesting.DriveTestApp;

public class PhoneStateListenerService extends Service {

	private static final  String TAG = "PhonestateListnerService";

	private static final String NO_SIGNAL_STRENGTH = "-";

	private TelephonyManager telephonyManager = null;
	private BroadcastReceiver connectivityReceiver = null;
	private IntentFilter networkStateChangedFilter;	
	private PhoneStateListenerImpl phoneStateListener = null;
	private DriveTestApp application; 

	@Override
	public void onCreate() {		
		super.onCreate();

		application = (DriveTestApp)getApplication();

		phoneStateListener = new PhoneStateListenerImpl();
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);	
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | 
				PhoneStateListener.LISTEN_DATA_ACTIVITY |
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |				
				PhoneStateListener.LISTEN_CELL_LOCATION |
				PhoneStateListener.LISTEN_CALL_STATE |				
				PhoneStateListener.LISTEN_SERVICE_STATE;

		telephonyManager.listen(phoneStateListener, events);

		networkStateChangedFilter = new IntentFilter();
		networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		connectivityReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {				
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
					application.setConnectionState(false);
					application.setNetworkState("No connection");
					application.setNetworkType("No network");

					if (networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE )) { 
						/*||networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {*/
						String networkType = networkInfo.getSubtypeName();
						Log.d(TAG, "connectivity network type: "+networkType);
						application.setNetworkType(networkType);

						if (networkInfo.isConnected()) {
							application.setConnectionState(true);
							application.setNetworkState("Connected");
						}
					}
				}
			}
		};

		registerReceiver(connectivityReceiver, networkStateChangedFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Toast.makeText(getBaseContext(), "PhoneState destroy", Toast.LENGTH_LONG).show();
		// stop listening to phone state changes
		if (telephonyManager != null){  
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);	
		}
		// stop the receiver
		if (connectivityReceiver != null) {
			unregisterReceiver(connectivityReceiver);
		}
	}

	class PhoneStateListenerImpl extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
			super.onSignalStrengthsChanged(signalStrength);
			String dbm = "";
			switch (telephonyManager.getPhoneType()){
			case TelephonyManager.PHONE_TYPE_CDMA:
				dbm = String.valueOf(signalStrength.getCdmaDbm());
				application.setSignalStrength(dbm);
				Log.d(TAG, " Signal strength changed:  CdmaDbm" + dbm);
				break;
			case TelephonyManager.PHONE_TYPE_GSM:
				int asu = signalStrength.getGsmSignalStrength();
				if (99 != asu) {					
					dbm = String.valueOf( -113 + 2 *  asu);
					application.setSignalStrength(dbm);
					Log.d(TAG, " Signal strength changed:  " + dbm +" Dbm");
				} else {
					// asu = 99 is a special case, where the signal strength is unknown.
					application.setSignalStrength("-");
					Log.d(TAG, " Signal strength changed: unknown");
				}
				
				application.setSignalLevel(getLevel(asu));
				break;
			case TelephonyManager.PHONE_TYPE_NONE:
			default:
				application.setSignalStrength(NO_SIGNAL_STRENGTH);
				Log.d(TAG, " Signal strength changed, but phone type is unknown!");
				break;
			}

			application.setCdmaEcio(String.valueOf(signalStrength.getCdmaEcio()));
			application.setEvdoDbm(String.valueOf(signalStrength.getEvdoDbm()));
			application.setEvdoEcio(String.valueOf(signalStrength.getEvdoEcio()));
			application.setEvdoSnr(String.valueOf(signalStrength.getEvdoSnr()));
			application.setGsmBitErrorRate(String.valueOf(signalStrength.getGsmBitErrorRate()));

			//Toast.makeText(context, "Signal strength changed!  ", Toast.LENGTH_SHORT).show();
		}				  
		public int getLevel(int asu) {
			int level = DriveTestApp.SIGNAL_UNKOWN;
			// ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
			// asu = 0 (-113dB or less) is very weak
			// signal, its better to show 0 bars to the user in such cases.
			// asu = 99 is a special case, where the signal strength is unknown.
			if  (asu >= DriveTestApp.GSM_SIGNAL_STRENGTH_GREAT) {
				level = DriveTestApp.SIGNAL_GREAT;
			} else if (asu >= DriveTestApp.GSM_SIGNAL_STRENGTH_GOOD) {
				level = DriveTestApp.SIGNAL_GOOD;
			} else if (asu >= DriveTestApp.GSM_SIGNAL_STRENGTH_MODERATE) {
				level = DriveTestApp.SIGNAL_MODERATE;
			}  else level = DriveTestApp.SIGNAL_WEAK;

			Log.d(TAG, " Signal Level= " + level);
			return level;
		}

		@Override
		public void onCallStateChanged(final int state, final String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch(state)
			{
			case TelephonyManager.CALL_STATE_IDLE:  
				application.setCallState("Idle"); 
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				application.setCallState("Ringing (" + incomingNumber + ")"); 
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:       
				application.setCallState("Offhook"); 
				break;
			default: 
				application.setCallState("Unkonwn");
			}
			Log.d(TAG, "phoneState updated");
		}

		private String getLAC(){
			GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
			String lac = "0";
			if (cellLocation != null){
				lac = String.format(Locale.getDefault(), "%d", cellLocation.getLac());	
			}
			return lac;
		}

		private String getCID(){
			GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
			String cid = "0";
			if (cellLocation != null){
				cid = String.format(Locale.getDefault(), "%d", cellLocation.getCid()& 0xffff);	
			}
			return cid;
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

		@Override
		public void onCellLocationChanged(CellLocation location)	{			
			super.onCellLocationChanged(location);
			Log.d(TAG, "onCellLocationChanged ");
			application.setCellLocation(getMCC(), getMNC(), getLAC(), getCID());
		}

		private String getDataState(int state){			
			switch (state){
			case TelephonyManager.DATA_CONNECTED :
				return "Connected";				
			case TelephonyManager.DATA_CONNECTING:
				return "Connecting";				
			case TelephonyManager.DATA_DISCONNECTED :
				return "Disconnected";
			case TelephonyManager.DATA_SUSPENDED: 
				return "Suspended";				
			}
			return "";
		}

		@Override
		public void onDataConnectionStateChanged(int state)	{
			super.onDataConnectionStateChanged(state);
			Log.d(TAG, "onDataConnectionStateChanged " + state);
			application.setDataConnectionState(getDataState(state));		
		}

		private String getDataActivity(int activityType){		
			switch (activityType){
			case TelephonyManager.DATA_ACTIVITY_NONE :
				return "None";				
			case TelephonyManager.DATA_ACTIVITY_IN:
				return "In";				
			case TelephonyManager.DATA_ACTIVITY_OUT :
				return "Out";				
			case TelephonyManager.DATA_ACTIVITY_INOUT: 
				return  "InOut";				
			case TelephonyManager.DATA_ACTIVITY_DORMANT:
				return  "Dormant";				
			}
			return "";
		}

		@Override
		public void onDataActivity(int direction)	{
			super.onDataActivity(direction);
			Log.d(TAG, "onDataActivity " + direction);
			application.setDataConnectionDirection(getDataActivity(direction));		
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState)
		{
			super.onServiceStateChanged(serviceState);
			switch(serviceState.getState())
			{
			case ServiceState.STATE_IN_SERVICE:     
				application.setServiceState("In service");
				Log.d(TAG, "onServiceStateChanged: in service ");
				break;
			case ServiceState.STATE_EMERGENCY_ONLY:         
				application.setServiceState("Emergency only"); 
				Log.d(TAG, "onServiceStateChanged: emergency");
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				application.setServiceState("Out of service");
				Log.d(TAG, "onServiceStateChanged: out of service");
				break;
			case ServiceState.STATE_POWER_OFF: 
				application.setServiceState("Power off");
				Log.d(TAG, "onServiceStateChanged: power off");
				break;
			default: 
				application.setServiceState("Unknown");
				Log.d(TAG, "onServiceStateChanged: unknown ");
				break;
			}				
		}	
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}


}

