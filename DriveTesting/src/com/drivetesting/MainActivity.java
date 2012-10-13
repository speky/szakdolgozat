package com.drivetesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  {

	private ListView telephonyManagerOutput = null;
	static final  String TAG = "MainActivity";
	
	String[] from = new String[] {"name", "value"};
	int[] to = new int[] { R.id.column_name, R.id.column_value};
	List<HashMap<String, String>> telephonyOverview  = new ArrayList<HashMap<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		telephonyManagerOutput = (ListView)findViewById(R.id.listview);

				
		// prepare the list of all records
		/*List<HashMap<String, String>> telephonyOverview  = new ArrayList<HashMap<String, String>>();
		for(int i = 0; i < 10; i++){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", "" + i);
			map.put("value", "col_1_item_" + i);		            
			telephonyOverview .add(map);

		}*/
		
	}
	@Override
	public void onStart() {
		super.onStart();

        // TelephonyManager
       final TelephonyManager  telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //this.telephonyManagerOutput.setText(telephonyManager.toString());

        // PhoneStateListener
        PhoneStateListener phoneStateListener = new PhoneStateListener() {

            @Override
            public void onCallStateChanged(final int state, final String incomingNumber) {
            	//telephonyManagerOutput.setText(getPhoneOverview(telephonyManager));
                Log.d(TAG, "phoneState updated - incoming number - " + incomingNumber);
            }
        };
		// Register the listener with the telephony manager
        telephonyManager.listen(phoneStateListener, 
        		PhoneStateListener.LISTEN_CALL_STATE|
        		PhoneStateListener.LISTEN_CELL_LOCATION|
        		PhoneStateListener.LISTEN_DATA_ACTIVITY|
        		PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR|
        		PhoneStateListener.LISTEN_DATA_CONNECTION_STATE|
        		PhoneStateListener.LISTEN_SERVICE_STATE|
        		PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            getPhoneOverview(telephonyManager);

     // fill in the grid_item layout
  		SpecialAdapter adapter = new SpecialAdapter(this, telephonyOverview, R.layout.grid, from, to);

   		telephonyManagerOutput.setAdapter(adapter);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Parse TelephonyManager values into a human readable String.
	 * 
	 * @param telephonyManager
	 * @return
	 */
	public void getPhoneOverview(final TelephonyManager telephonyManager) {

		telephonyOverview.clear();
		
		String phoneType = "Ismeretlen";
		switch (telephonyManager.getPhoneType()){
		case TelephonyManager.PHONE_TYPE_NONE : phoneType = "NONE";
		break;
		case TelephonyManager.PHONE_TYPE_GSM : phoneType = "GSM";
		break;
		case TelephonyManager.PHONE_TYPE_CDMA : phoneType = "CDMA";
		break;
		case TelephonyManager.PHONE_TYPE_SIP : phoneType = "SIP";
		break;
		}
		String phoneNumber = telephonyManager.getLine1Number();
		String deviceId = telephonyManager.getDeviceId();
		String deviceSoftwareVersion = telephonyManager.getDeviceSoftwareVersion();

		String simCountryIso = telephonyManager.getSimCountryIso();
		String simOperator = telephonyManager.getSimOperator();
		String MCC = "";
		String MNC = "";
		if (simOperator != null) {
			MCC= simOperator.substring(0, 3);
			MNC = simOperator.substring(3);
		}
		String simOperatorName = telephonyManager.getSimOperatorName();
		String simSerialNumber = telephonyManager.getSimSerialNumber();
		String simSubscriberId = telephonyManager.getSubscriberId();

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
		String roaming = "Nincs";
		if (telephonyManager.isNetworkRoaming()){
			roaming = "Van";
		}

		String networkCountryIso = telephonyManager.getNetworkCountryIso();
		String networkOperator = telephonyManager.getNetworkOperator();
		String networkOperatorName = telephonyManager.getNetworkOperatorName();

		String networkType = "";
		switch (telephonyManager.getNetworkType()){
		case TelephonyManager.NETWORK_TYPE_UNKNOWN :
			networkType = "Ismeretlen"; 
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			networkType = "CDMA"; 
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE :
			networkType = "EDGE"; 
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			networkType = "EHRPD"; 
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			networkType = "EVDO_0"; 
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			networkType = "EVDO_A"; 
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			networkType = "EVDO_B"; 
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS: 
			networkType = "GPRS"; 
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA: 
			networkType = "GPRS"; 
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			networkType = "HSPA"; 
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			networkType = "HSUPA"; 
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			networkType = "iDen"; 
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			networkType = "LTE"; 
			break;
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			networkType = "1xRTT"; 
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			networkType = "UMTS"; 
			break;
		} 

		String dataActivity = "";
		switch (telephonyManager.getDataActivity ()){
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

		String dataState = "";
		switch (telephonyManager.getDataState()){
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

		GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		String cellLocationString = "0 0";
		if (cellLocation != null){
			cellLocationString = cellLocation.getLac() + " " + cellLocation.getCid();
		}

		
		addHashMapElement("Telefon típusa", phoneType);
		addHashMapElement("Telefon szám ", phoneNumber);
		addHashMapElement("IMEI\\ESN ", deviceId);
		addHashMapElement(" IMEI\\SV ", deviceSoftwareVersion);
		addHashMapElement("Ország kód ", simCountryIso);
		addHashMapElement("MCC ", MCC);
		addHashMapElement("MNC ", MNC);
		addHashMapElement("Operátor ", simOperatorName);
		addHashMapElement("SIM állapota ", simStateString);
		addHashMapElement("SIM SN ", simSerialNumber);
		addHashMapElement("IMSI ", simSubscriberId);
		addHashMapElement("Roaming ", roaming);
		addHashMapElement("Adatkapcsolat ", dataActivity);
		addHashMapElement("Kapcsolat állapota ", dataState);
		addHashMapElement("cellLocationString ", cellLocationString);
		addHashMapElement("networkCountry ", networkCountryIso);
		addHashMapElement("networkOperator ", networkOperator);
		addHashMapElement("Operator Name ", networkOperatorName);
		addHashMapElement("Hálózat típusa ", networkType);                
		
	}
	
	private void addHashMapElement(String key, String value){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", key);
		map.put("value", value);		            
		telephonyOverview .add(map);		
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
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

		default:
			return false;			
		}
	}

	public void onResume() {
		super.onResume();

		SharedPreferences prefs = ((DriveTestApp)getApplication()).prefs;

		/*checkbox.setText(new Boolean(prefs.getBoolean("checkbox", false)).toString());
		ringtone.setText(prefs.getString("ringtone", "<unset>"));
		checkbox2.setText(new Boolean(prefs.getBoolean("checkbox2", false)).toString());
		//text.setText(prefs.getString("text", "<unset>"));
		list.setText(prefs.getString("list", "<unset>"));
		 */

	}

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
	};

	public class SpecialAdapter extends SimpleAdapter {
		private int[] colors = new int[] { 0x30FF0000, 0x300000FF };

		public SpecialAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			int colorPos = position % colors.length;
			view.setBackgroundColor(colors[colorPos]);
			return view;
		}
	}

}
