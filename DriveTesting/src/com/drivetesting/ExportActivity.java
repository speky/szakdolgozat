package com.drivetesting;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ExportActivity extends Activity {

	private TextView testIdText; 
	private EditText fileText; 
	private long testId = 0;
	private String fileName = "";
	private SharedPreferences sharedPreferences;
	private final String TESTID = "TestId";
	private final String FILE = "file";	
	private FileHandler testHandler = null;
	
	public void setFileHandler(FileHandler handler) {
		testHandler = handler;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Export");

		sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		
		testIdText = (TextView)findViewById(R.id.editTestId);
		fileText = (EditText)findViewById(R.id.output_file_name);
		fileText.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	            fileName = s.toString();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
	}

	private void save() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(TESTID, testId);
		editor.putString(FILE, fileName);
		editor.commit();
	}

	private void load() {	    	    
		testId = sharedPreferences.getLong(TESTID, 0);
		setTestIdString();
		fileName = sharedPreferences.getString(FILE, "");
		fileText.setText(fileName);
	}
	
	private void setTestIdString() {
		if (testId == 0) {
			testIdText.setText(getString(R.string.choosetestid));
		} else if (testId == -1) {
			testIdText.setText(getString(R.string.testid) + " ALL");
		} else {
			testIdText.setText(getString(R.string.testid) + " "+ Long.toString(testId));
		}
	}
	
	public void onResume() {
		super.onResume();
		load();
	}
	
	public void onPause() {
		super.onPause();
		save();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_export).setVisible(false);
		return true;
	}
	
	private void showAlarm(final String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //do things
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void onExportClick(View view) {
		
		if (testId == 0) {
			showAlarm("Choose Test ID!");
			return;
		}
		
		String fileName = fileText.getText().toString();
		if (fileName.length() == 0) {
			showAlarm("Set file name!");
			return;
		}
		
		ExportToCVS export = new ExportToCVS(this, ((DriveTestApp)getApplication()).getDataStorage(), testId, fileName, testHandler);
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
				try{
					testId = Long.parseLong(items[item].toString());
				}catch (NumberFormatException ex) {
					// ALL is selected
					testId = -1;
				}
				setTestIdString();
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

	private final String DIRECTORY = "export";
	private FileHandler fileHandler;
	private final ProgressDialog dialog;
	private Context context ;
	private DataStorage dbData;
	private long testId;
	
	ExportToCVS(Context context, DataStorage dbData, long testId, String fileName, FileHandler testHandler) {		 
		this.context = context;
		this.dbData = dbData;
		this.testId = testId;
		dialog = new ProgressDialog(context);
		if (testHandler == null) {
			fileHandler = new FileHandler(context, fileName, DIRECTORY);
		}
	}

	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Exporting database...");
		this.dialog.show();
	}

	protected Boolean doInBackground(final String... args) {
		List<DbData> datas = null;
		if (testId == -1) {
			datas = dbData.queryAll();
		}else {		
			datas = dbData.querySpecifiedTest(String.valueOf(testId));
		}
		
		if (datas.size() == 0) {
			return false;
		}

		
		String exportText = dbData.getColunNames();

		exportText += "\n";
		Log.d("EXPORTER: ", "header=" + exportText);
						
		for (int i  = 0; i < datas.size(); ++i) {
			exportText += datas.get(i).toString()+ "\n";		
		}
		
		if (fileHandler.writeFile(true, exportText, true)) {
			return true;
		}
		
		return false;
	}

	protected void onPostExecute(final Boolean success) {
		if (this.dialog.isShowing()) { 
			this.dialog.dismiss(); 
		}if (success) {
			Toast.makeText(context, "Export successful! File: "+ fileHandler.getFilePath(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show();
		}
	}
}
