package com.drivetesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHandler {

	private final String TAG = "Android File Handler: "; 
	private boolean isAbleToWriteExternalStorage = false;
	private  String fileName = "log.txt";
	private File externalDir = null;

	public FileHandler(Context context, final String fileName) {
		this.fileName = fileName;

		if (isExternalStorageWritable()) {
			isAbleToWriteExternalStorage = true;
		}

		externalDir = getFileStorageDir(context, "DriveTestFiles");
	}

	public void info(String message) {

	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getFileStorageDir(Context context, String dirName) {
		// Get the directory for the app's private pictures directory. 
		File file = new File(context.getExternalFilesDir(null), dirName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		return file;
	}

	public boolean fileIsExist(final String fileName) {
		if  (externalDir != null) {
			File file = new File(externalDir, fileName);
			if (file !=null) {
				return file.exists();
			}
		}
		return false;
	}

	public boolean deleteFile(final String fileName) {
		if  (externalDir != null) {
			File file = new File(externalDir, fileName);
			if (file !=null) {
				file.delete();
				Log.i(TAG, "File deleted: "+fileName);
			}
		}
		return false;
	}

	public boolean writeFile(final String fileName, final String text) {
		if (externalDir == null) {
			return false;
		}

		try {
			File file = new File(externalDir, fileName);
			if (file.exists() == false){
				file.createNewFile();
			}
			OutputStream os = new FileOutputStream(file);
			OutputStreamWriter outWriter = new OutputStreamWriter(os);
			outWriter.append(text);
			outWriter .flush();
			outWriter.close();
			os.close();
			return true;
		}
		catch ( IOException ex){
			Log.e(TAG, ex.getMessage());
		}
		return false;

	}
	
	public String readFile(final String fileName){
		try {
			File file = new File(externalDir, fileName);
			FileInputStream in = new FileInputStream(file);
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
