package com.drivetesting;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {
	private HttpClient httpClient = null; 
	private Context context = null;
	private RadioGroup directionGroup;
	private int directionGroupIndex;
	private final String DIRECTION_GROUP = "DirectionGroup";
	private SharedPreferences sharedPreferences;
	
	private Handler handler = new Handler(Looper.getMainLooper()) { 
		@Override 
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			((TextView)findViewById(R.id.editOut)).setText(msg.toString());		    	
		} 
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Test");

		setContentView(R.layout.activity_test);
		context = this;
		
		String valiable=((DriveTestApp)getApplication()).getServerIp();
		
		sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		
		directionGroup = (RadioGroup)findViewById(R.id.dir_group);
		directionGroupIndex = directionGroup.getCheckedRadioButtonId();		
		/*int uid = getApplication().getApplicationInfo().uid;
		uid = android.os.Process.myUid();
		long txApp = TrafficStats.getUidTxBytes(uid);
		long rxApp = TrafficStats.getUidRxBytes(uid);
		 */
		
		httpClient = new HttpClient(context, handler);
	}
	
	public void onStartTestClick(View view) {
		new Thread(httpClient).start();	
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

}