package com.drivetesting;

import http.testhandler.HttpParser;
import http.testhandler.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.drivetesting.observers.TestObserver;
import com.drivetesting.services.HttpService;

public class TestActivity extends Activity implements TestObserver, AsyncResponse {

	private final String TAG = "TestActivity: ";
	private final String DIRECTION_GROUP = "DirectionGroup";
	private final String TYPE_GROUP = "TypeGroup";
	private final String MESSAGE = "message";
	
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
	private void showSettingsGPSAlert() {
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

	private void showSettingsNetworkAlert() {
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
		
		progressBar.setVisibility(ProgressBar.VISIBLE);
		findViewById(R.id.bt_startTest).setEnabled(false);
		findViewById(R.id.bt_stopTest).setEnabled(true);
		
		if (application.controlPort == 0) {
			ConnectToServer  task = new ConnectToServer(application.ServerPort, application.getServerIp());		
			task.delegate = this;		
			task.execute("");
		}
		else {
			processFinish(application.controlPort);
		}
	}

	public void processFinish(int output){
		application.controlPort = output;
		int direction = directionGroupIndex == R.id.dir_dl ? DriveTestApp.DOWNLOAD : DriveTestApp.UPLOAD;
		int type = typeGroupIndex == R.id.type_tcp ? DriveTestApp.TCP : DriveTestApp.UDP;		
		application.startHttpClientService(direction, type);
	}

	public class ConnectToServer extends AsyncTask<String, Void, Integer>{
		public AsyncResponse delegate=null;
		private int port;
		private String ip;

		ConnectToServer(int serverPort, String serverIP){
			port = serverPort;
			ip = serverIP;
		}

		protected Integer doInBackground(final String... args) {
			try {			
				Socket socket = new Socket();				 
				socket.connect(new InetSocketAddress(ip, port));	

				String msg ="INVITE / HTTP*/1.0\nEND\n";
				// send message to the server
				PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
				printWriter.println(msg);
				printWriter.flush();

				StringBuffer buffer = new StringBuffer();
				Scanner scanner = new Scanner(socket.getInputStream());
				while (scanner.hasNextLine()) {
					String readedLine = scanner.nextLine();
					if (readedLine.compareTo("END") !=  0) {
						buffer.append("+"+readedLine);
					}else{
						Log.d(TAG, "Receive message from server: "+ buffer.toString());
						break;
					}
				}		
				HttpParser parser = new HttpParser(new Logger(""));
				parser.parseHttpMessage(buffer.toString());
				printWriter.close();
				scanner.close();
				socket.close();
				socket = null;
				if (parser.getMethod().equals("INVITE")) {
					return Integer.parseInt(parser.getHeadProperty("PORT"));					
				}			
			} catch (Exception e) {
				Log.d(TAG, "ERROR in doInBackground() " + e.getMessage());
			}
			return 0;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			delegate.processFinish(result);
		}
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
		SaveFileTask task = new SaveFileTask(this, messageList);
		task.execute("");
	}

	public void onExitClick(View view) {
		Toast.makeText(this, "Test session released!", Toast.LENGTH_SHORT).show();
		application.stopHttpClientService();
		progressBar.setVisibility(ProgressBar.INVISIBLE);		
		findViewById(R.id.bt_startTest).setEnabled(true);
		findViewById(R.id.bt_stopTest).setEnabled(false);
		
		try {
			if (HttpService.socket != null) {
				HttpService.socket.close();
				HttpService.socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		application.controlPort = 0;
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
		editor.commit();
	}

	private void load() {	    	    
		int index = sharedPreferences.getInt(TYPE_GROUP, R.id.type_tcp);
		if (index != 0) {
			typeGroupIndex = index;
			typeGroup.check(typeGroupIndex);
		}

		index = sharedPreferences.getInt(DIRECTION_GROUP, R.id.dir_dl);
		if (index != 0) {
			directionGroupIndex = index;
			directionGroup.check(directionGroupIndex);
		}

		setMessage(sharedPreferences.getString(MESSAGE, ""));
		
		findViewById(R.id.bt_startTest).setEnabled(!application.isTestRunning());
		findViewById(R.id.bt_stopTest).setEnabled(application.isTestRunning());		
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

class SaveFileTask extends AsyncTask<String, Void, Boolean> {

	private final String DIRECTORY = "logs";
	private FileHandler fileHandler;
	private final ProgressDialog dialog;
	private Context context ;	
	private String fileName;
	private List<HashMap<String, String>> log;

	SaveFileTask(Context context, List<HashMap<String, String>> logs) {		 
		this.context = context;
		log = logs;
		dialog = new ProgressDialog(context);

		fileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'", Locale.getDefault()).format(new Date());
		fileHandler = new FileHandler(context, fileName, DIRECTORY);		
	}

	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Saving log...");
		this.dialog.show();
	}

	protected Boolean doInBackground(final String... args) {
		if (fileName.equals("")) {
			return false;
		}

		String logString =  "";

		for (HashMap<String, String> map : log) {
			for (HashMap.Entry<String, String> entry : map.entrySet()) {
				logString  += entry.getValue()+ "\n";
			}			
		}
		if (fileHandler.writeFile(true, logString, true)) {
			return true;
		}
		return false;
	}

	protected void onPostExecute(final Boolean success) {
		if (this.dialog.isShowing()) { 
			this.dialog.dismiss();			
		}if (success) {
			Toast.makeText(context, "Log saving successful! File: "+ fileHandler.getFilePath(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Log saving failed", Toast.LENGTH_SHORT).show();
		}
	}
}
