package com.drivetesting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private final String TAG = "PrefsActivity";
	private SharedPreferences prefs;
	private DriveTestApp app;

	private static final Pattern IP_ADDRESS = Pattern.compile(
			"((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
					+ "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
					+ "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
					+ "|[1-9][0-9]|[0-9]))");

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		app = (DriveTestApp)getApplication();
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragmentImpl()).commit();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		prefs = app.getSharedPreference();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");		
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}


	public static boolean isIPValid(String address) {
		boolean isValid = false;		    		    
		Matcher matcher = IP_ADDRESS.matcher(address);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences pref,	String key) {
		this.prefs = pref;		
		Log.d(TAG, "On Change preferences: "+ key);
		if (key.equals("serverIp")) {
			String adr = pref.getString("serverIp", null);
			if (isIPValid(adr) == false) {			
				Toast.makeText(this, "IP address is invalid! It is reset to default.", Toast.LENGTH_LONG).show();
				SharedPreferences.Editor prefEditor = pref.edit();
				prefEditor.putString(key, "92.249.132.6");
				prefEditor.commit();
				reload();	             
			}
			Log.d(TAG, "Server IP has changed");			
		}
	}

	//  It relaunches the activity using the same intent that fired it
	private void reload(){
		startActivity(getIntent()); 
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_settings).setVisible(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id)
		{
		case R.id.menu_test:
			startActivity(new Intent(this, TestActivity.class));
			return true;

		case R.id.menu_export:
			startActivity(new Intent(this, ExportActivity.class));
			return true;

		case R.id.menu_map:
			startActivity(new Intent(this, OSMActivity.class));
			return true;

		case R.id.menu_main:
			startActivity(new Intent(this, MainActivity.class));
			return true;

		default:
			return false;			
		}
	}


}
