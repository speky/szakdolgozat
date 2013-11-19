package com.drivetesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHandler {

	private final String TAG = "Android File Handler: "; 
	private boolean externalStorageAvailable = false;
	private boolean externalStorageWriteable = false;
	private  String fileName;
	private File externalFile = null;
	private OutputStreamWriter outWriter;
	private FileOutputStream fileStream;
	
	public FileHandler(Context context, final String fileName, final String directory) {
		this.fileName = fileName;

		init();

		if (externalStorageWriteable) {
			getExternalFile(context, directory);
		}
	}

	private void init() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    externalStorageAvailable = true;
		    externalStorageWriteable = false;
		} else {
			//we can neither read nor write
		    externalStorageAvailable = externalStorageWriteable = false;
		}
	}

	private void getExternalFile(Context context, final String dirName) {
		// get directory where the application should store it's files
		File root = context.getExternalFilesDir(null); 
	    
	    File dir = new File (root.getAbsolutePath() + "/" + dirName);
	    dir.mkdirs();
	    externalFile = new File(dir, fileName);
	    
	}
	
	public void deleteExternalFile() {
		if  (externalFile != null && externalFile.exists()) {
			externalFile.delete();
		}
	}

	public void closeExternalWrite() {
		try{
			if (null != outWriter) {
				outWriter.close();
			}
			if (null != fileStream) {
				fileStream.close();
			}
		}catch (Exception e) {
			//Toast.makeText(getBaseContext(), e.getMessage(),	Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage());
		}
	}
	
	public boolean writeExternalFile(final String text, boolean clean) {
		if (externalFile == null) {
			return false;
		}
		
		if (clean) {
			deleteExternalFile();
		}

		try {			
			fileStream = new FileOutputStream(externalFile);
			outWriter = new OutputStreamWriter(fileStream);
			outWriter.append(text);
			outWriter .flush();
			
			return true;
		}
		catch ( IOException ex){
			Log.e(TAG, ex.getMessage());
		}
		return false;

	}
	
	public String readExternalFile(){
		try {			
			FileInputStream in = new FileInputStream(externalFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(in));
			String dataRow = "";
			String buffer = "";
			while ((dataRow = myReader.readLine()) != null) {
				buffer += dataRow + "\n";
			}
			//txtData.setText(aBuffer);
			myReader.close();
			//Toast.makeText(getBaseContext(),		"Done reading SD 'mysdfile.txt'",		Toast.LENGTH_SHORT).show();
			return buffer;
		} catch (Exception e) {
			//Toast.makeText(getBaseContext(), e.getMessage(),	Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
}
