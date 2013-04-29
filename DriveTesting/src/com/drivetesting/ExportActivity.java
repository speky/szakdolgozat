package com.drivetesting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_export).setVisible(false);
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

		case R.id.menu_main:
			startActivity(new Intent(this, MainActivity.class));
			return true;

		case R.id.menu_map:
			startActivity(new Intent(this, OSMActivity.class));
			return true;

		case R.id.menu_test:
			startActivity(new Intent(this, TestActivity.class));
			return true;

		default:
			return false;			
		}
	}
}
