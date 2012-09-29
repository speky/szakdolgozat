package com.drivetesting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  {

	private TextView checkbox = null;
	private TextView ringtone = null;
	private TextView checkbox2 = null;
	private TextView text = null;
	private TextView list = null;

	TelephonyManager telephonyManager;
	PhoneStateListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_drive_test);

		checkbox=(TextView)findViewById(R.id.checkbox);
		ringtone=(TextView)findViewById(R.id.ringtone);
		checkbox2=(TextView)findViewById(R.id.checkbox2);
		text=(TextView)findViewById(R.id.text);
		list=(TextView)findViewById(R.id.list);

		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

		// Create a new PhoneStateListener
		listener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				String stateString = "N/A";
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					stateString = "Idle";
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					stateString = "Off Hook";
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					stateString = "Ringing";
					break;
				}
				text.append(String.format("\nonCallStateChanged: %s", stateString));
			}
			public void onCellLocationChanged(CellLocation location) {
				GsmCellLocation gsmLocation = (GsmCellLocation)location;
				Toast.makeText(getApplicationContext(), String.valueOf(gsmLocation.getCid()), Toast.LENGTH_LONG).show();
			}
		};

		// Register the listener wit the telephony manager
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_CELL_LOCATION);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
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

		checkbox.setText(new Boolean(prefs.getBoolean("checkbox", false)).toString());
		ringtone.setText(prefs.getString("ringtone", "<unset>"));
		checkbox2.setText(new Boolean(prefs.getBoolean("checkbox2", false)).toString());
		//text.setText(prefs.getString("text", "<unset>"));
		list.setText(prefs.getString("list", "<unset>"));

	}


}
