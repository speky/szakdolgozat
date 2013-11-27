package com.drivetesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

import com.drivetesting.observers.PhoneStateObserver;

public class MainActivity extends Activity implements PhoneStateObserver  {

	private static final  String TAG = "MainActivity";

	private String[] from = new String[] {"name", "value"};
	private int[] to = new int[] { R.id.column_name, R.id.column_value};

	private List<HashMap<String, String>> phoneDataList  = null;
	private List<HashMap<String, String>> networkDataList  = null;

	private SeparatedListAdapter separatedAdapter = null;
	private SimpleAdapter phoneDataAdapter = null;
	private SimpleAdapter networkDataAdapter = null;	
	private TelephonyManager  telephonyManager = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(false) ;
			actionBar.setTitle("Main");
		}

		setContentView(R.layout.main_tab);
						               
		phoneDataList  = new ArrayList<HashMap<String, String>>();
		networkDataList  = new ArrayList<HashMap<String, String>>();
		
		// fill in the grid_item layout			
		phoneDataAdapter = new SimpleAdapter(this, phoneDataList, R.layout.grid, from, to);
		networkDataAdapter = new SimpleAdapter(this, networkDataList, R.layout.grid, from, to);		

		// create our list and custom adapter  
		separatedAdapter = new SeparatedListAdapter(this);
		separatedAdapter.addSection(this.getString(R.string.phone_header), phoneDataAdapter);  
		separatedAdapter.addSection(this.getString(R.string.network_header), networkDataAdapter );

		ListView list = (ListView)findViewById(R.id.listview);
		list.setAdapter(separatedAdapter);

		initPhoneData();
		initNetworkData();

		phoneDataAdapter.notifyDataSetChanged();
		networkDataAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}

	// just for testing purposes
	public  List<HashMap<String, String>>  getPhoneDataList() {
		return phoneDataList;
	}
	// just for testing purposes
	public  List<HashMap<String, String>>  getNetworkDataList() {
		return networkDataList;
	}

	@Override
	protected void onPause() {
		super.onPause();	
		Log.d(TAG, "onPause");
		((DriveTestApp)getApplication()).removePhoneStateObserver(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume ");		
		((DriveTestApp)getApplication()).registerPhoneStateObserver(this);	 
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
		return telephonyManager.getNetworkCountryIso().toUpperCase(Locale.getDefault());
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

	private void initPhoneData() {
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		phoneDataList.clear();				
		setHashMapElement(phoneDataList, "Phone type", getPhoneType());
		setHashMapElement(phoneDataList, "Phone number", getTelephoneNumber());
		setHashMapElement(phoneDataList, "IMEI/ESN", getIMEI());
		setHashMapElement(phoneDataList, "Manufacturer", Build.MANUFACTURER);
		setHashMapElement(phoneDataList, "Model", Build.MODEL);
		setHashMapElement(phoneDataList, "SIM state", getSimState());
		setHashMapElement(phoneDataList, "SIM SN", getSimSerialNumber());
		String str = getSoftwareVersion();
		if (str == null) {
			str = "-";
		}
		setHashMapElement(phoneDataList, "Software version", str);
		setHashMapElement(phoneDataList, "IMSI", getSubscriberId());
		setHashMapElement(phoneDataList, "Signal strength", "-");
		setHashMapElement(phoneDataList, "Cdma EcIo", "-");
		setHashMapElement(phoneDataList, "Evdo dBm", "-");
		setHashMapElement(phoneDataList, "Evdo EcIo", "-");
		setHashMapElement(phoneDataList, "Evdo SNR", "-");
		setHashMapElement(phoneDataList, "Bit error rate", "-");
	}

	private void initNetworkData() {
		networkDataList.clear();
		setHashMapElement(networkDataList, "Operator", getNetworkOperatorName());
		setHashMapElement(networkDataList, "Country", getNetworkCountry());
		setHashMapElement(networkDataList, "Roaming", getRoaming());
		setHashMapElement(networkDataList, "Call state", "-");
		setHashMapElement(networkDataList, "Service state", "-");
		setHashMapElement(networkDataList, "Network type", "-");
		setHashMapElement(networkDataList, "Network state", "-");
		setHashMapElement(networkDataList, "Data direction", "-");
		setHashMapElement(networkDataList, "Data connection state", "-");
		setHashMapElement(networkDataList, "MNC", "-");
		setHashMapElement(networkDataList, "MCC", "-");
		setHashMapElement(networkDataList, "LAC", "-");
		setHashMapElement(networkDataList, "CID", "-");
	}

	//location 
	//provider
	// gps accuracy

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
		setHashMapElement(phoneDataList, "Signal strength", value + getApplicationContext().getString(R.string.unit_dbm));
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
		Log.d(TAG, "DATA DIR UPDATE");
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
}
