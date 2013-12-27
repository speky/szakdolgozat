package com.drivetesting;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;

import com.drivetesting.observers.TestObserver;

public class TestActivity extends Activity implements TestObserver {
	
	private final String TAG = "TestActivity: ";
	private final String DIRECTION_GROUP = "DirectionGroup";
	private final String TYPE_GROUP = "TypeGroup";
	private final String MESSAGE = "message";
	private final String STARTON = "start";
	private final String DIRECTORY = "logs";
	
	private RadioGroup directionGroup = null;
	private int directionGroupIndex = 0;	
	private RadioGroup typeGroup = null;
	private int typeGroupIndex = 0;
		
	private ProgressBar progressBar = null;
	
	private SharedPreferences sharedPreferences;
	
	private String[] from = new String[] { "value"};
	private int[] to = new int[] {R.id.column_value};

	private List<HashMap<String, String>> messageList  = null;
	private SeparatedListAdapter separatedAdapter = null;
	private SimpleAdapter messageAdapter = null;
	
	private DriveTestApp application = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = ((DriveTestApp)getApplication());
		
		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Test");
				
		setContentView(R.layout.activity_test);

		messageList  = new ArrayList<HashMap<String, String>>();					
		messageAdapter = new SimpleAdapter(this, messageList, R.layout.grid_test, from, to);
		// create list and custom adapter  
		separatedAdapter = new SeparatedListAdapter(this);
		separatedAdapter.addSection(this.getString(R.string.message_header), messageAdapter);		
		ListView list = (ListView)findViewById(R.id.listview);
		list.setAdapter(separatedAdapter);		
		messageAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
		
		sharedPreferences = getPreferences(Context.MODE_PRIVATE);

		directionGroup = (RadioGroup)findViewById(R.id.dir_group);
		directionGroupIndex = directionGroup.getCheckedRadioButtonId();		

		typeGroup = (RadioGroup)findViewById(R.id.type_group);
		typeGroupIndex = typeGroup.getCheckedRadioButtonId();
    	
		progressBar = ((ProgressBar)findViewById(R.id.progressBar));
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		
		findViewById(R.id.bt_startTest).setEnabled(true);
		findViewById(R.id.bt_stopTest).setEnabled(false);
	
	}
	 
	// Function to show settings dialog       
	public void showSettingsGPSAlert() {
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
	
	public void showSettingsNetworkAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("Network settings");

		// Setting Dialog Message
		alertDialog.setMessage("Network is not enabled. Do you want to go to settings menu?");

		// Setting Icon to Dialog
		alertDialog.setIcon(android.R.drawable.ic_delete);

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
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
		if (application.isInternetConnectionActive() == false) {	        
			showSettingsNetworkAlert();
			return;
		}
		final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false ) {	        
			showSettingsGPSAlert();
			return;
		}	    
		((DriveTestApp)getApplication()).startGPSService();
		int direction = directionGroupIndex == R.id.dir_dl ? DriveTestApp.DOWNLOAD : DriveTestApp.UPLOAD;
		int type = typeGroupIndex == R.id.type_tcp ? DriveTestApp.TCP : DriveTestApp.UDP;
		progressBar.setVisibility(ProgressBar.VISIBLE);
		findViewById(R.id.bt_startTest).setEnabled(false);
		findViewById(R.id.bt_stopTest).setEnabled(true);
		application.startHttpClientService(direction, type);
	}
		
	public void onStopTestClick(View view) {
		Log.d(TAG, "Stop test");
		application.stopHttpClientService();
		progressBar.setVisibility(ProgressBar.INVISIBLE);		
		findViewById(R.id.bt_startTest).setEnabled(true);
		findViewById(R.id.bt_stopTest).setEnabled(false);
	}

	private void setMessage(String message){
		messageList.clear();		
		StringTokenizer tokens = new StringTokenizer(message, "\n");
		
		while (tokens.hasMoreTokens()) {
			String line = tokens.nextToken();
			// add new element
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("value", line);
			messageList .add(map);
		}
		messageAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	
	private void deleteMessages() {
		messageList.clear();
		messageAdapter.notifyDataSetChanged();
		separatedAdapter.notifyDataSetChanged();
	}
	
	public void onClearClick(View view) {
		deleteMessages();
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
	}

	@Override
	public void onPause() {
		super.onPause();
		application.removeReportObserver(this);
		save();
	}

	@Override
	public void onResume() {
		super.onResume();
		application.registerReportObserver(this);	
		
		load();
		
		setMessage(application.getTestMessage());
		
		if (application.isTestRunning()) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
		} else {
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}
	}

	private void save() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(DIRECTION_GROUP, directionGroupIndex);
		editor.putInt(TYPE_GROUP, typeGroupIndex);
	//	editor.putString(MESSAGE, messageList.get(0).get("value"));
		editor.putBoolean(STARTON, findViewById(R.id.bt_startTest).isEnabled());
		editor.commit();
	}

	private void load() {	    	    
		typeGroupIndex = sharedPreferences.getInt(TYPE_GROUP, R.id.type_tcp);
		typeGroup.check(typeGroupIndex);
		
		directionGroupIndex = sharedPreferences.getInt(DIRECTION_GROUP, R.id.dir_dl);
		directionGroup.check(directionGroupIndex);
		
		setMessage(sharedPreferences.getString(MESSAGE, ""));
		
		Boolean isStarted = sharedPreferences.getBoolean(STARTON, true);
		findViewById(R.id.bt_startTest).setEnabled(isStarted);
		findViewById(R.id.bt_stopTest).setEnabled(!isStarted);		
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

	@Override
	public void update(int action, String str) {
		setMessage(str) ;
    	// error happened or test ended
    	if (action == 0) {  		
    		progressBar.setVisibility(ProgressBar.INVISIBLE);
    		findViewById(R.id.bt_startTest).setEnabled(true);
    		findViewById(R.id.bt_stopTest).setEnabled(false);    		
    	}		
	}

}