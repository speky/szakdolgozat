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
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity  {

	private static final  String TAG = "MainActivity";
	
	private CustomPhoneStateListener phoneStateListener = null;
	
	private String[] from = new String[] {"name", "value"};
	private int[] to = new int[] { R.id.column_name, R.id.column_value};
	private List<HashMap<String, String>> phoneDataList  = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> networkDataList  = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> dataList  = new ArrayList<HashMap<String, String>>();
	private SeparatedListAdapter separatedAdapter = null;
	private SimpleAdapter phoneDataAdapter = null;
	private SimpleAdapter networkDataAdapter = null;
	
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Main"); 

		setContentView(R.layout.main_tab);
			
		phoneStateListener = new CustomPhoneStateListener();
		phoneStateListener.startSignalLevelListener();

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

		
		refreshNetworkDataList();
	}	

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");		
		// Unregister the listener with the telephony manager
		phoneStateListener.stopListening();
		unregisterReceiver(batteryInfoReceiver);
		super.onPause();	
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume ");
		super.onResume();
		
		// Register the listener with the telephony manager
		phoneStateListener.startSignalLevelListener();

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
		List<NeighboringCellInfo> NeighboringList = telephonyManager.getNeighboringCellInfo();
		addHashMapElement(networkDataList, "Neighboring List", "Lac : Cid : RSSI");
		for(int i=0; i < NeighboringList.size(); i++){
			String dBm;
			int rssi = NeighboringList.get(i).getRssi();
			if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
				dBm = "Unknown RSSI";
			}else{
				dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
			}
			String neighboring = String.valueOf(NeighboringList.get(i).getLac()) +" : "+ String.valueOf(NeighboringList.get(i).getCid()) +" : "
									+ dBm +"\n";
			addHashMapElement(networkDataList, "", neighboring);
		}

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

	public class IncomingReceiver extends BroadcastReceiver{
		 
	    @Override
	    public void onReceive(Context context, Intent intent) {
	 
	        if (intent.getAction().equals(GPSService.CUSTOM_INTENT)) {
	                System.out.println("*****GOT THE INTENT********");
	 
	        }
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

}
