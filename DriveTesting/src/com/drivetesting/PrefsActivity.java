package com.drivetesting;

import java.util.List;

import android.content.Intent;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class PrefsActivity extends PreferenceActivity {
	
	  @Override
	  public void onBuildHeaders(List<Header> target) {
		  loadHeadersFromResource(R.xml.preference_headers, target);
	  }
	
	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.menu, menu);
			menu.findItem(R.id.menu_settings).setVisible(false);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			int id = item.getItemId();
			switch (id)
			{
			case R.id.menu_test:
				startActivity(new Intent(this, TestActivity.class));
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
