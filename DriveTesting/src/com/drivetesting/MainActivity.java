package com.drivetesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity  {

	private static final  String TAG = "MainActivity";
	private static final int EXCELLENT_LEVEL = 75;
	private static final int GOOD_LEVEL = 50;
	private static final int MODERATE_LEVEL = 25;
	private static final int WEAK_LEVEL = 0;
	private final String NO_SIGNAL_STRENGTH = "?";

	private Context context = null;
	private TelephonyManager telephonyManager = null;
	private CustomPhoneStateListener phoneStateListener = null;
	private BroadcastReceiver connectivityBroadcastReceiver = null;
	private IntentFilter networkStateChangedFilter;

	private String[] from = new String[] {"name", "value"};
	private int[] to = new int[] { R.id.column_name, R.id.column_value};
	private List<HashMap<String, String>> phoneDataList  = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> networkDataList  = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> dataList  = new ArrayList<HashMap<String, String>>();
	private SeparatedListAdapter separatedAdapter = null;
	private SimpleAdapter phoneDataAdapter = null;
	private SimpleAdapter networkDataAdapter = null;
	private  boolean isNetworkConnected = false;

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

	private static final int MIN_BATTERY_LEVEL = 20;
	private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 
	        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
	            int level = intent.getIntExtra("level", 0);
	            int scale = intent.getIntExtra("scale", 100);
	            int plugged = intent.getIntExtra("plugged", 0);

	            if(plugged == 0){
	                if((level * 100 / scale) < MIN_BATTERY_LEVEL){
	                    // do something...
	                }
	            }
	        }
	    }
	};
	
	public List<HashMap<String, String>> getPhoneData() {
		return phoneDataList;  
	}

	public List<HashMap<String, String>> getNetworkData() {
		return networkDataList;  
	}

	private void startSignalLevelListener() {
		if (telephonyManager == null){  
			telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		}
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | 
				PhoneStateListener.LISTEN_DATA_ACTIVITY |
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |				
				PhoneStateListener.LISTEN_CELL_LOCATION |
				PhoneStateListener.LISTEN_CALL_STATE |				
				PhoneStateListener.LISTEN_SERVICE_STATE;

		telephonyManager.listen(phoneStateListener, events);
	}

	private void stopListening(){
		if (telephonyManager == null){  
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);	
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

	public  boolean isInternetConnectionActive() {
		return isNetworkConnected;
	}

	public  void setConnectionState(boolean connectionState ) {
		isNetworkConnected = connectionState;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Main"); 

		networkStateChangedFilter = new IntentFilter();
		networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		connectivityBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					NetworkInfo info = ((ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
					String mTypeName = info.getTypeName();
					networkType = info.getSubtypeName();
					Log.i(TAG, "*** Network Type: " + mTypeName 
							+ ", subtype: " + networkType);
					//updateScreen();
				}
			}
		};

		setContentView(R.layout.main_tab);

		context = this;		
		phoneStateListener = new CustomPhoneStateListener();
		startSignalLevelListener();

		// Registers BroadcastReceiver to track network connection changes.
		//connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();		
		//this.registerReceiver(connectivityBroadcastReceiver, filter);

		// fill in the grid_item layout			
		phoneDataAdapter = new SimpleAdapter(this, phoneDataList, R.layout.grid, from, to);
		networkDataAdapter = new SimpleAdapter(this, networkDataList, R.layout.grid, from, to);		

		// create our list and custom adapter  
		separatedAdapter = new SeparatedListAdapter(this);
		separatedAdapter .addSection(this.getString(R.string.phone_header), phoneDataAdapter);  
		separatedAdapter .addSection(this.getString(R.string.network_header), networkDataAdapter );

		ListView list = (ListView)findViewById(R.id.listview);
		list.setAdapter(separatedAdapter);

		refreshPhoneDataList();
		refreshNetworkDataList();
	}	

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();		
		// Unregister the listener with the telephony manager
		stopListening();
		unregisterReceiver(batteryInfoReceiver);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume ");
		super.onResume();

		registerReceiver(connectivityBroadcastReceiver, networkStateChangedFilter);
		// Register the listener with the telephony manager
		startSignalLevelListener();

		 registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
		
		/*SharedPreferences prefs = ((DriveTestApp)getApplication()).prefs;
		checkbox.setText(new Boolean(prefs.getBoolean("checkbox", false)).toString());
		ringtone.setText(prefs.getString("ringtone", "<unset>"));
		checkbox2.setText(new Boolean(prefs.getBoolean("checkbox2", false)).toString());
		//text.setText(prefs.getString("text", "<unset>"));
		list.setText(prefs.getString("list", "<unset>"));
		 */
	}

	@Override
	protected void onDestroy(){
		Log.d(TAG, "onDestroy ");
		super.onDestroy();
		// Unregister the listener with the telephony manager
		stopListening();
	}

	private class CustomPhoneStateListener extends PhoneStateListener{
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
			super.onSignalStrengthsChanged(signalStrength);

			switch (telephonyManager.getPhoneType()){
			case TelephonyManager.PHONE_TYPE_CDMA:
				signalStrengthString = String.valueOf(signalStrength.getCdmaDbm()) + context .getString(R.string.unit_dbm); 
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

			Log.d(TAG, "^ Signal strength changed: " + signalStrength);
			Toast.makeText(getApplicationContext(), "Signal strength changed!  ", Toast.LENGTH_SHORT).show();
			refreshPhoneDataList();
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
			refreshNetworkDataList();
		}

		@Override
		public void onCellLocationChanged(CellLocation location)
		{			
			super.onCellLocationChanged(location);
			String locationString = location.toString();

			Log.d(TAG, "onCellLocationChanged " + locationString);
			//Toast.makeText(getApplicationContext(), "Cell location changed!  ", Toast.LENGTH_SHORT).show();
			refreshNetworkDataList();
		}


		@Override
		public void onDataConnectionStateChanged(int state)
		{
			super.onDataConnectionStateChanged(state);
			Log.d(TAG, "onDataConnectionStateChanged " + state);
			dataConnectionState = getDataState(state);
			refreshNetworkDataList();
		}

		@Override
		public void onDataActivity(int direction)
		{
			super.onDataActivity(direction);
			Log.d(TAG, "onDataActivity " + direction);

			dataDirection = getDataActivity(direction);
			refreshNetworkDataList();
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
			refreshNetworkDataList();
		}
	};

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

	public void refreshPhoneDataList() {

		phoneDataList.clear();
		//Phone		
		addHashMapElement(phoneDataList, "Phone Type", getPhoneType());
		addHashMapElement(phoneDataList, "Telefon szám ", getTelephoneNumber());
		addHashMapElement(phoneDataList, "IMEI\\ESN ", getIMEI());
		addHashMapElement(phoneDataList, "Gyártó ", Build.MANUFACTURER);
		addHashMapElement(phoneDataList, "Modell", Build.MODEL);
		addHashMapElement(phoneDataList, "SIM állapota ", getSimState());
		addHashMapElement(phoneDataList, "SIM SN ", getSimSerialNumber());
		addHashMapElement(phoneDataList, "Software version ", getSoftwareVersion());
		addHashMapElement(phoneDataList, "IMSI ", getSubscriberId());
		addHashMapElement(phoneDataList, "Jelerõség ", signalStrengthString);
		addHashMapElement(phoneDataList, "Bit hiba ráta", gsmBitErrorRate);
		addHashMapElement(phoneDataList, "CDMA EcIo", cdmaEcio);
		addHashMapElement(phoneDataList, "EVDO dBm", evdoDbm);
		addHashMapElement(phoneDataList, "EVDOEcIi", evdoEcio);
		addHashMapElement(phoneDataList, "SNR", evdoSnr);

		//location 
		//sitedb
		//provider
		// latitude
		// longitude
		//altitude
		//site bearing
		//speed
		// gps accuracy
		// site latitude
		//site longitude
		//distance to site

		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}

	public void refreshNetworkDataList() {

		networkDataList.clear();
		// Network		
		addHashMapElement(networkDataList, "Operátor ", getNetworkOperatorName());
		addHashMapElement(networkDataList, "Hálózat típusa ", networkType);
		addHashMapElement(networkDataList, "Hálózat állapota", networkState);
		addHashMapElement(networkDataList, "Service state ", serviceStateString);
		addHashMapElement(networkDataList, "Ország ", getNetworkCountry());
		addHashMapElement(networkDataList, "MCC ", getMCC());
		addHashMapElement(networkDataList, "MNC ", getMNC());
		addHashMapElement(networkDataList, "LAC", getLAC());
		addHashMapElement(networkDataList, "CellID", getCID());
		addHashMapElement(networkDataList, "Adatkapcsolat ", dataDirection);
		addHashMapElement(networkDataList, "Kapcsolat állapota ", dataConnectionState);
		addHashMapElement(networkDataList, "Roaming ", getRoaming());
		addHashMapElement(networkDataList, "Hívás állapot", callState );
		//Neighbouring cell infos		
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}

	private void addHashMapElement(List<HashMap<String, String>> dataList, String key, String value){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", key);
		map.put("value", value);		            
		dataList .add(map);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_main).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id)
		{
		case R.id.menu_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;

		case R.id.menu_export:
			startActivity(new Intent(this, ExportActivity.class));
			return true;

		case R.id.menu_map:
			startActivity(new Intent(this, OSMActivity.class));
			return true;

		case R.id.menu_test:
			startActivity(new Intent(this, TestActivity.class));
			return true;

		default:
			return false;			
		}
	}
	
	
	/*
	public void onClick(View view) {
		Intent intent = new Intent(this, DownloadService.class);
		// Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		intent.setData(Uri.parse("http://www.vogella.com/index.html"));
		intent.putExtra("urlpath", "http://www.vogella.com/index.html");
		startService(intent);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Object path = message.obj;
			if (message.arg1 == RESULT_OK && path != null) {
				Toast.makeText(MainActivity.this,
						"Downloaded" + path.toString(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(MainActivity.this, "Download failed.",
						Toast.LENGTH_LONG).show();
			}

		};
	};*/
	/*
	public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "*** connectivity onreceive! ");
			ConnectivityManager connectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE ) {
				networkType = networkInfo.getSubtypeName();
				Log.d(TAG, "connectivity network_Type: "+networkType);
				if (networkInfo.isConnected()){
					setConnectionState(true);
					networkState = "Kapcsolódva";					
					Toast.makeText(context, R.string.network_connected, Toast.LENGTH_SHORT).show();
				} else {
					setConnectionState(false);
					networkState = "Nincs kapcsolat";
					Toast.makeText(context, R.string.connection_lost, Toast.LENGTH_SHORT).show();
				}
				refreshNetworkDataList();
			}			
		}
	};*/

}
