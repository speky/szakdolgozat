package com.drivetesting;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class TestActivity extends Activity implements Observer {
	
	private final String TAG = "TestActivity: ";
	private final String DIRECTION_GROUP = "DirectionGroup";
	private final String TYPE_GROUP = "TypeGroup";
					
	private RadioGroup directionGroup = null;
	private int directionGroupIndex = 0;	
	private RadioGroup typeGroup = null;
	private int typeGroupIndex = 0;

	private EditText text = null;
	
	private ProgressBar progressBar = null;
	
	private SharedPreferences sharedPreferences;
	private NotificationManager notificationManager = null;
	
	private DriveTestApp application = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = ((DriveTestApp)getApplication());
		
		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Test");
				
		setContentView(R.layout.activity_test);

		sharedPreferences = getPreferences(Context.MODE_PRIVATE);

		directionGroup = (RadioGroup)findViewById(R.id.dir_group);
		directionGroupIndex = directionGroup.getCheckedRadioButtonId();		

		typeGroup = (RadioGroup)findViewById(R.id.type_group);
		typeGroupIndex = typeGroup.getCheckedRadioButtonId();
		
		text = ((EditText)findViewById(R.id.editOutput));
		text.setText("") ;
    	
		progressBar = ((ProgressBar)findViewById(R.id.progressBar));
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	 
	// Function to show settings dialog       
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// Setting Icon to Dialog
		alertDialog.setIcon(android.R.drawable.ic_delete);

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);				
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
	
	public void onStartTestClick(View view) {
		final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false ) {	        
			showSettingsAlert();
			return;
		}
		((DriveTestApp)getApplication()).startGPSService();
		int direction = directionGroupIndex == R.id.dir_dl ? DriveTestApp.DOWNLOAD : DriveTestApp.UPLOAD;
		int type = typeGroupIndex == R.id.type_tcp ? DriveTestApp.TCP : DriveTestApp.UDP;
		application.startHttpClientService(direction, type);
		progressBar.setVisibility(ProgressBar.VISIBLE);	
	}
		
	public void onStopTestClick(View view) {
		Log.d(TAG, "Stop test");
		application.stopHttpClientService();
		progressBar.setVisibility(ProgressBar.INVISIBLE);
	}

	public void onClearClick(View view) {
		text.setText(" ".toCharArray(), 0, 1);
		application.clearTestMessage();
	}
	
	public void onSaveClick(View view) {
		
	}
	
	public void onDirectionChoosed(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		if (checked) {
			directionGroupIndex = directionGroup.getCheckedRadioButtonId();
		}
	}

	public void onTypeChoosed(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		if (checked) {
			typeGroupIndex = typeGroup.getCheckedRadioButtonId();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();		
		notificationManager.cancel("Drive Test Notification", 0);//R.string.local_service_started);
	}

	@Override
	public void onPause() {
		super.onPause();
		application.removeObserver(this);
		save();
	}

	@Override
	public void onResume() {
		super.onResume();
		application.registerObserver(this);	
		
		load();
		
		text.setText(application.getTestMessage());
		
		if (application.isTestRunning()) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
		} else {
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}
	}

	private void save() {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();	    
		editor.putInt(DIRECTION_GROUP, directionGroupIndex);
		editor.putInt(TYPE_GROUP, typeGroupIndex);
		editor.commit();
	}

	private void load() {	    	    
		typeGroupIndex = sharedPreferences.getInt(TYPE_GROUP, R.id.type_group);
		typeGroup.check(typeGroupIndex);
		
		directionGroupIndex = sharedPreferences.getInt(DIRECTION_GROUP, R.id.dir_dl);
		directionGroup.check(directionGroupIndex);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_test).setVisible(false);
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

		case R.id.menu_main:
			startActivity(new Intent(this, MainActivity.class));
			return true;

		default:
			return false;			
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	/*private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = "Drive Test Notification";//getText(R.string.local_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,	new Intent(this, TestActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.service_name),		text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		notificationManager.notify(R.string.local_service_started, notification);
	}*/

	@Override
	public void update(int action, String str) {
    	text.setText(str) ;
    	if (action == 1) {
    		progressBar.setVisibility(ProgressBar.VISIBLE);    		
    	}  	else {
    		progressBar.setVisibility(ProgressBar.INVISIBLE);    		
    	}
		
	}

}