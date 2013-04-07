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
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

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

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
	}

}