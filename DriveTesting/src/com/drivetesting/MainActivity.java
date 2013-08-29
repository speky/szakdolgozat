package com.drivetesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity implements PhoneStateObserver  {

	private static final  String TAG = "MainActivity";

	private String[] from = new String[] {"name", "value"};
	private int[] to = new int[] { R.id.column_name, R.id.column_value};
	
	private List<HashMap<String, String>> phoneDataList  = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> networkDataList  = new ArrayList<HashMap<String, String>>();
	
	private SeparatedListAdapter separatedAdapter = null;
	private SimpleAdapter phoneDataAdapter = null;
	private SimpleAdapter networkDataAdapter = null;
	
	private TelephonyManager  telephonyManager = null; 

	/*private static final int MIN_BATTERY_LEVEL = 20;
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
	};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Main"); 

		setContentView(R.layout.main_tab);

		// fill in the grid_item layout			
		phoneDataAdapter = new SimpleAdapter(this, phoneDataList, R.layout.grid, from, to);
		networkDataAdapter = new SimpleAdapter(this, networkDataList, R.layout.grid, from, to);		

		// create our list and custom adapter  
		separatedAdapter = new SeparatedListAdapter(this);
		separatedAdapter .addSection(this.getString(R.string.phone_header), phoneDataAdapter);  
		separatedAdapter .addSection(this.getString(R.string.network_header), networkDataAdapter );

		ListView list = (ListView)findViewById(R.id.listview);
		list.setAdapter(separatedAdapter);

		init();
	}	

	@Override
	protected void onPause() {
		super.onPause();	
		Log.d(TAG, "onPause");
		((DriveTestApp)getApplication()).removePhoneStateObserver(this);
		// Unregister the listener with the telephony manager
		//unregisterReceiver(batteryInfoReceiver);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume ");		
		((DriveTestApp)getApplication()).registerPhoneStateObserver(this);
		//registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
	}

	@Override
	protected void onDestroy(){		
		super.onDestroy();
		Log.d(TAG, "onDestroy ");
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
			simStateString = "No SIM";
			break;
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			simStateString = "Network locked";
			break;
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			simStateString = "PIN required";
			break;
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			simStateString = "PUK required";
			break;
		case TelephonyManager.SIM_STATE_READY:
			simStateString = "Ready";
			break;
		case TelephonyManager.SIM_STATE_UNKNOWN:
			simStateString = "Ismeretlen";
			break;
		}        
		return simStateString;
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

	private void init() {
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		phoneDataList.clear();				
		setHashMapElement(phoneDataList, "Phone type", getPhoneType());
		setHashMapElement(phoneDataList, "Phone number", getTelephoneNumber());
		setHashMapElement(phoneDataList, "IMEI\\ESN", getIMEI());
		setHashMapElement(phoneDataList, "Manufacturer", Build.MANUFACTURER);
		setHashMapElement(phoneDataList, "Model", Build.MODEL);
		setHashMapElement(phoneDataList, "SIM state", getSimState());
		setHashMapElement(phoneDataList, "SIM SN", getSimSerialNumber());
		setHashMapElement(phoneDataList, "Software version", getSoftwareVersion());
		setHashMapElement(phoneDataList, "IMSI", getSubscriberId());
		setHashMapElement(phoneDataList, "Signal strength", "-1");
		setHashMapElement(phoneDataList, "Cdma EcIo", "-1");
		setHashMapElement(phoneDataList, "Evdo dBm", "-1");
		setHashMapElement(phoneDataList, "Evdo EcIo", "-1");
		setHashMapElement(phoneDataList, "Evdo SNR", "-1");
		setHashMapElement(phoneDataList, "Bit error rate", "-1");
		
		networkDataList.clear();
		setHashMapElement(networkDataList, "Operator", getNetworkOperatorName());
		setHashMapElement(networkDataList, "Country", getNetworkCountry());
		setHashMapElement(networkDataList, "Roaming", getRoaming());
		setHashMapElement(networkDataList, "Call state", "-1");
		setHashMapElement(networkDataList, "Service state", "-1");
		setHashMapElement(networkDataList, "Network type", "-1");
		setHashMapElement(networkDataList, "Network state", "-1");		
		setHashMapElement(networkDataList, "Data direction", "-1");		
		setHashMapElement(networkDataList, "Data connection state", "-1");
		setHashMapElement(networkDataList, "MNC", "-1");
		setHashMapElement(networkDataList, "MCC", "-1");
		setHashMapElement(networkDataList, "LAC", "-1");
		setHashMapElement(networkDataList, "CID", "-1");
		
	}

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


	public void setNeighboring(String value) {
		//setHashMapElement(networkDataList, "Neighboring List", "Lac : Cid : RSSI");
		setHashMapElement(networkDataList, "Neighbouring list", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}/*	
	List<NeighboringCellInfo> NeighboringList = telephonyManager.getNeighboringCellInfo();

	for(int i=0; i < NeighboringList.size(); i++){
		String dBm;
		int rssi = NeighboringList.get(i).getRssi();
		if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
			dBm = "Unknown RSSI";
		}else{
			dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
		}
		String neighboring = String.valueOf(NeighboringList.get(i).getLac()) +" : "+ String.valueOf(NeighboringList.get(i).getCid()) +" : "	+ dBm +"\n";		
	}*/

	private int findElement(final String key, List<HashMap<String, String>> dataList) {
		for (int i = 0; i < dataList.size(); ++i) {
			String name = dataList.get(i).get("name");
			if (name.equals(key)) {
				return i;
			} 
		}		
		return -1;
	}

	private void setHashMapElement(List<HashMap<String, String>> dataList, String key, String value){
		int id = findElement(key, dataList);		
		if (dataList.isEmpty() ||  id == -1) {
			// add new element
			HashMap<String, String> map = new HashMap<String, String>();		
			map.put("name", key);
			map.put("value", value);		            
			dataList .add(map);
		} else {
			// modify
			dataList.get(id).put("value", value);
		}
	}

	@Override
	public void updateSignalStrength(String value) {
		setHashMapElement(phoneDataList, "Signal strength", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();	
	}
	@Override
	public void updateCdmaEcio(String value) {
		setHashMapElement(phoneDataList, "Cdma EcIo", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateEvdoDbm(String value) {
		setHashMapElement(phoneDataList, "Evdo dBm", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateEvdoEcio(String value) {
		setHashMapElement(phoneDataList, "Evdo EcIo", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateEvdoSnr(String value) {
		setHashMapElement(phoneDataList, "Evdo SNR", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateGsmBitErrorRate(String value) {
		setHashMapElement(phoneDataList, "Bit error rate", value);
		phoneDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void updateServiceState(String value) {
		setHashMapElement(networkDataList, "Service state", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateCallState(String value) {
		setHashMapElement(networkDataList, "Call state", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateNetworkState(String value) {
		setHashMapElement(networkDataList, "Network state", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateDataConnectionState(String value) {
		setHashMapElement(networkDataList, "Data connection state", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateDataDirection(String value) {
		setHashMapElement(networkDataList, "Data direction", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateNetworkType(String value) {
		setHashMapElement(networkDataList, "Network type", value);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	@Override
	public void updateCellLocation(String mnc, String mcc, String lac, String cid) {
		setHashMapElement(networkDataList, "MNC", mnc);
		setHashMapElement(networkDataList, "MCC", mcc);
		setHashMapElement(networkDataList, "LAC", lac);	
		setHashMapElement(networkDataList, "CID", cid);
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();	
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
/*public class IncomingReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(GPSService.CUSTOM_INTENT)) {
				System.out.println("*****GOT THE INTENT********");

			}
		}
	}*/

}

