package com.drivetesting;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class ExportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);
		
		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Export");
	}
	
	public void onResume() {
		super.onResume();
	}

}
