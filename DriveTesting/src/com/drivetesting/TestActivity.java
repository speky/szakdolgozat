package com.drivetesting;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class TestActivity extends Activity {
	private HttpClient httpClient; 
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		context = this;
		new Thread(httpClient = new HttpClient(context)).start();
	}

	public void StartTestClick(View view) {
			}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
	}

}