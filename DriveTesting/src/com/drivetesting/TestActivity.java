package com.drivetesting;


import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
	protected static final String TAG = "TestActivity: ";
	private Context context = null;
	private RadioGroup directionGroup;
	private ProgressBar progressBar;
	private int directionGroupIndex;
	private final String DIRECTION_GROUP = "DirectionGroup";
	private SharedPreferences sharedPreferences;
	private NotificationManager notificationManager;
	private EditText text;
	private StringBuilder textOut;
	private boolean progressbarVisible = false;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Test");
				
		setContentView(R.layout.activity_test);
		context = this;

		sharedPreferences = getPreferences(Context.MODE_PRIVATE);

		directionGroup = (RadioGroup)findViewById(R.id.dir_group);
		directionGroupIndex = directionGroup.getCheckedRadioButtonId();		
		/*int uid = getApplication().getApplicationInfo().uid;
		uid = android.os.Process.myUid();
		long txApp = TrafficStats.getUidTxBytes(uid);
		long rxApp = TrafficStats.getUidRxBytes(uid);
		 */
		textOut = new StringBuilder();
		text = ((EditText)findViewById(R.id.editOutput));
		text.setText("") ;
    	
		progressBar = ((ProgressBar)findViewById(R.id.progressBar));
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		
		//doBindService();
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	 
	public void onStartTestClick(View view) {
		((DriveTestApp)getApplication()).startHttpClientService();
		progressBar.setVisibility(ProgressBar.VISIBLE);
	
	}
		
	public void onStopTestClick(View view) {
		((DriveTestApp)getApplication()).stopHttpClientService();
		progressBar.setVisibility(ProgressBar.INVISIBLE);
	}

	public void onClearClick(View view) {
		text.setText(" ".toCharArray(), 0, 1);
		textOut.delete(0, textOut.length());
	}
	
	public void onSaveClick(View view) {
		
	}
	
	public void onDirectionChoosed(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		directionGroupIndex = directionGroup.getCheckedRadioButtonId();
		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.dir_dl:
			if (checked)
				// 
				break;
		case R.id.dir_ul:
			if (checked)
				// 
				break;
		}
	}

	public void onTypeChoosed(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.type_tcp:
			if (checked)
				// 
				break;
		case R.id.type_udp:
			if (checked)
				// 
				break;
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
		save();
	}

	@Override
	public void onResume() {
		super.onResume();
		load();
		if (((DriveTestApp)getApplication()).isTestRunning()) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
		} else {
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}
		text.setText(textOut) ;
	}

	private void save() {
		sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();	    
		editor.putInt(DIRECTION_GROUP, directionGroupIndex);
		editor.commit();
		Toast.makeText(this, "Save", Toast.LENGTH_LONG).show();
	}

	private void load() {	    	    
		directionGroupIndex = sharedPreferences.getInt(DIRECTION_GROUP, R.id.dir_dl);
		directionGroup.check(directionGroupIndex);
		Toast.makeText(this, "Load", Toast.LENGTH_LONG).show();		
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
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		/*CharSequence text = "Drive Test Notification";//getText(R.string.local_service_started);

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
		notificationManager.notify(R.string.local_service_started, notification);*/
	}

	@Override
	public void update(int action, String str) {		
		textOut.insert(0, str+"\n\n");
    	text.setText(textOut) ;
    	if (action == 1) {
    		progressBar.setVisibility(ProgressBar.VISIBLE);
    		progressbarVisible = true;
    	}  	else {
    		progressBar.setVisibility(ProgressBar.INVISIBLE);
    		progressbarVisible = false;
    	}
		
	}

}