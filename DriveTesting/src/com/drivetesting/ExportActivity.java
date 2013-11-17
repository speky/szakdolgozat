package com.drivetesting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ExportActivity extends Activity {

	private TextView testIdText; 
	private long testId = 0; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Export");

		testIdText = (TextView)findViewById(R.id.editTestId);
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

	public void onExportClick(View view) {		
		ExportToCVS export = new ExportToCVS(this, ((DriveTestApp)getApplication()).getDataStorage(), testId);
		export.execute("");
	}

	public void onTestClick(View view) {
		List<String> list = ((DriveTestApp)getApplication()).getTestIds();
		list.add("All");
		final CharSequence[] items = list.toArray(new CharSequence[list.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Test ID");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// load road for the testId            	
				testId = Long.parseLong(items[item].toString());            	
				if (testId == 0) {
					testIdText.setText("Test id: Undefined");
				} else {
					testIdText.setText("Test id:" + Long.toString(testId));
				}
			}                
		});
		AlertDialog alert = builder.create();
		alert.show();
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

class ExportToCVS extends AsyncTask<String, Void, Boolean> {
	private final ProgressDialog dialog;

	private Context context ;
	private DataStorage dbData;
	private long testId;

	ExportToCVS(Context context, DataStorage dbData, long testId) {		 
		this.context = context;
		this.dbData = dbData;
		this.testId = testId;
		dialog = new ProgressDialog(context);
	}

	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Exporting database...");
		this.dialog.show();
	}


	protected Boolean doInBackground(final String... args) {

		List<DbData> datas = dbData.querySpecifiedTest(String.valueOf(testId));
		if (datas.size() == 0) {
			return true;
		}

		File exportDir = new File(Environment.getExternalStorageDirectory(), "");
		if (!exportDir.exists()) { 
			exportDir.mkdirs(); 
		}

		File file = new File(exportDir, "myfile.csv");
		Boolean returnCode = true;
		try {
			file.createNewFile();
			String csvHeader = dbData.getColunNames();
			/*for (i = 0; i < GC.CURCOND_COLUMN_NAMES.length; i++) {
                if (csvHeader.length() > 0) {
                    csvHeader += ",";
                }
                csvHeader += "\"" + GC.CURCOND_COLUMN_NAMES[i] + "\"";
            }*/

			csvHeader += "\n";
			Log.d("EXPORTER: ", "header=" + csvHeader);

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fileWriter);
			try {				
				String csvValues = "";			
				out.write(csvHeader);
				for (int i  = 0; i < datas.size(); ++i) {
					csvValues = datas.get(i).toString()+ "\n";
					out.write(csvValues);
				}
			} catch (IOException e) {
				returnCode = false;
				Log.d("EXPORT", "IOException: " + e.getMessage());
			}
			finally {
				out.close();
			}         
			return returnCode;
		} catch (IOException e) {
			Log.e("MainActivity", e.getMessage(), e);
			return false;
		}
	}

	protected void onPostExecute(final Boolean success) {
		if (this.dialog.isShowing()) { 
			this.dialog.dismiss(); 
		}

		if (success) {
			Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show();
		}
	}
}
