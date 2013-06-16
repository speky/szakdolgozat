package com.drivetesting;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class DriveTestApp extends Application implements OnSharedPreferenceChangeListener {

	static final String TAG = "DriveTesting";

	private DataStorage dataStorage;
	private SharedPreferences prefs;	
	private String serverIp;	

	private boolean isGpsServiceRun = false;
	private int activeGpsActivity = 0;
	private Context context = null;

	private ReportTask task = new ReportTask(); 
	private HttpBroadcastReceiver receiver;

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		serverIp= prefs.getString("serverIp", "0.0.0.0");

		dataStorage = new DataStorage(this);
		Log.d(TAG, "App created");

		// start location service 
		context = getApplicationContext();
		Intent service = new Intent(context, GPSService.class);
		startService(service);

		IntentFilter filter = new IntentFilter(HttpBroadcastReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new HttpBroadcastReceiver();
		registerReceiver(receiver, filter);
		
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

	
	 public String getIPAddress(boolean useIPv4) {
	        try {
	            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	            for (NetworkInterface intf : interfaces) {
	                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
	                for (InetAddress addr : addrs) {
	                    if (!addr.isLoopbackAddress()) {
	                        String sAddr = addr.getHostAddress().toUpperCase();
	                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
	                        if (useIPv4) {
	                            if (isIPv4) 
	                                return sAddr;
	                        } else {
	                            if (!isIPv4) {
	                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
	                            return delim<0 ? sAddr : sAddr.substring(0, delim);
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception ex) { } // for now eat exceptions
	    return "";
	}

	public boolean startHttpClientService() {		
		Intent httpIntent = new Intent(this, HttpClient.class);
		httpIntent.putExtra(HttpClient.PARAM_OWN_IP, getIPAddress(true));		
		startService(httpIntent);
		return true;
	}

	public boolean stopHttpClientService() {
		Intent httpIntent = new Intent(this, HttpClient.class);		
		stopService(httpIntent);
		return true;
	}
	
	public void setGpsService(boolean isRun) {
		isGpsServiceRun = true;
	}
	
	public void activeGpsActivity() {
		++activeGpsActivity;
	}

	public void deactiveGspActivity() {
		--activeGpsActivity;
	}

	public String getServerIp() {
		return serverIp;
	}

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences pref,	String key) {
		this.prefs = pref;
		Log.d(TAG, "On Change preferences: "+ key);
		if (key.equals("serverIp")) {
			//Preference connectionPref = findPreference(key);
			Log.d(TAG, "Server IP changed");
		}

	}

	class ReportTask extends TimerTask {
		public void run() {

			System.out.println("timer update");
		}
	}


}
