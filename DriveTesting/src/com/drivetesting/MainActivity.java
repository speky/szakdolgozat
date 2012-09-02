package com.drivetesting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView checkbox = null;
	private TextView ringtone = null;
	private TextView checkbox2 = null;
	private TextView text = null;
	private TextView list = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
			setContentView(R.layout.activity_drive_test);
						
			checkbox=(TextView)findViewById(R.id.checkbox);
			ringtone=(TextView)findViewById(R.id.ringtone);
			checkbox2=(TextView)findViewById(R.id.checkbox2);
			text=(TextView)findViewById(R.id.text);
			list=(TextView)findViewById(R.id.list);
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.menu, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			switch (item.getItemId())
			{
			case R.id.menu_settings:
				startActivity(new Intent(this, PrefsActivity.class));
				return true;

			case R.id.menu_export:
				startActivity(new Intent(this, ExportActivity.class));
				return true;
				
			default:
				return false;			
			}
		}

		public void onResume() {
			super.onResume();
			
			SharedPreferences prefs = ((DriveTestApp)getApplication()).prefs;
			
			checkbox.setText(new Boolean(prefs.getBoolean("checkbox", false)).toString());
			ringtone.setText(prefs.getString("ringtone", "<unset>"));
			checkbox2.setText(new Boolean(prefs.getBoolean("checkbox2", false)).toString());
			text.setText(prefs.getString("text", "<unset>"));
			list.setText(prefs.getString("list", "<unset>"));

		}
	

}
