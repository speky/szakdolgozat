package com.drivetesting;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {
	private HttpClient httpClient = null; 
	private Context context = null;
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

		/*int uid = getApplication().getApplicationInfo().uid;
		uid = android.os.Process.myUid();
		long txApp = TrafficStats.getUidTxBytes(uid);
		long rxApp = TrafficStats.getUidRxBytes(uid);
		 */

		httpClient = new HttpClient(context, handler);
	}

	/*onRestoreInstanceState(Bundle savedInstanceState)
	 * This method is called after onStart() when the activity is 
	 * being re-initialized from a previously saved state, given 
	 * here in savedInstanceState. Most implementations will simply 
	 * use onCreate(Bundle) to restore their state, but it is 
	 * sometimes convenient to do it here after all of the 
	 * initialization has been done or to allow subclasses 
	 * to decide whether to use your default implementation. 
	 * The default implementation of this method performs a restore 
	 * of any view state that had previously 
	 * been frozen by onSaveInstanceState(Bundle). */
	String sNewMyText = "";
	int nNewMyInt = 0;
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		sNewMyText = savedInstanceState.getString("my_text");
		nNewMyInt = savedInstanceState.getInt("my_int");
		Toast.makeText(this, "onRestoreInstanceState()", Toast.LENGTH_LONG).show();
		Log.i("onRestoreInstanceState", "onRestoreInstanceState()");	 
	}


	public void onStartTestClick(View view) {
		new Thread(httpClient).start();	
	}

	public void onDirectionChoosed(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

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

	/*onPause() called as part of the activity lifecycle when an activity 
	 * is going into the background, but has not (yet) been killed. 
	 * The counterpart to onResume().
	 *The Activity can store preferences  SharedPreferences.Editor */
	 @Override
	 protected void onPause() {
		 super.onPause();
		 Toast.makeText(this, "onPause()", Toast.LENGTH_LONG).show();
		 Log.i("onPause", "onPause()");
	 }
	 
	 /*onSaveInstanceState(Bundle outState) called to retrieve per-instance 
	  * state from an activity before being killed so that the state can be 
	  * restored in onCreate(Bundle) or onRestoreInstanceState(Bundle) 
	  * (the Bundle populated by this method will be passed to both). */
	 
	 String sMyText = "some text";
	 int nMyInt = 10;
	 
	 @Override
	 protected void onSaveInstanceState(Bundle outState) {
		 // Save away the original text, so we still have it if the activity
		 // needs to be killed while paused.
		 outState.putString("my_text", sMyText);
		 outState.putInt("my_int", nMyInt);
		 Toast.makeText(this, "onSaveInstanceState()", Toast.LENGTH_LONG).show();
		 Log.i("onSaveInstanceState", "onSaveInstanceState()");
	 }
	 
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
	}

}