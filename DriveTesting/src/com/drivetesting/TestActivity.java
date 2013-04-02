package com.drivetesting;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

public class TestActivity extends Activity {
	private HttpClient httpClient = null; 
	private Context context = null;
	private Handler handler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		context = this;
		handler = new Handler() { 
			@Override 
			public void handleMessage(Message msg) {
				((TextView)findViewById(R.id.editOut)).setText(msg.toString());		    	
			} 
		};
		
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