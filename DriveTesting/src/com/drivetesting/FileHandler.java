package com.drivetesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private Context context;
	private String directory;
	private String filePath = "";

	// for testing porpuses
	public void setExternalStorageWriteable(boolean value) {
		externalStorageWriteable = value;
	}
	
	//for testing porpuses
	public void setExternalStorageAvailable(boolean value) {
		externalStorageAvailable = value;
	}
	
	public FileHandler(Context context, final String fileName, final String directory) {
		this.fileName = fileName;
		this.context = context;
		this.directory = directory;
		init();
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

	public String getFilePath() {
		return filePath;
	}
	
	private boolean getExternalFile() {
		// get directory where the application should store it's files
		File root = context.getExternalFilesDir(null); 
		File dir = null;
		// get the path of the application's directory 
		if (directory.length() == 0) {
			dir = new File (root.getAbsolutePath());
		} else {
			dir = new File (root.getAbsolutePath() + "/" + directory);
		}

		if (dir.exists() == false) {
			if (externalStorageWriteable) {
				dir.mkdirs();	    	    	
			} else {
				return false;
			}
		}
		externalFile = new File(dir, fileName);
		
		return true;
	}

	public void deleteExternalFile() {
		if  (externalFile == null) {
			if (getExternalFile() == false) {
				return ;
			}
		}
		externalFile.delete();		
	}
		
	public boolean writeFile(boolean externalPreferred, final String text, boolean clean) {
		if (externalPreferred && externalStorageWriteable) {
			return writeExternalFile(text, clean);
		}
		return writeInternalFile(text, clean);
	}
	
	public boolean writeExternalFile(final String text, boolean clean) {
		if (externalFile == null) {
			if (getExternalFile() == false) {
				Log.d("D", "gebasz");
				return false;
			}
		}

		filePath = externalFile.getAbsolutePath();
		
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
		finally{
			try{
				outWriter.close();				
				fileStream.close();				
			}catch (Exception e) {
				//Toast.makeText(getBaseContext(), e.getMessage(),	Toast.LENGTH_SHORT).show();
				Log.e(TAG, e.getMessage());
			}			
		}

		return false;
	}

	public boolean writeInternalFile(final String text, boolean clean) {

		int flag = Context.MODE_APPEND;
		if (clean) {
			flag = Context.MODE_PRIVATE;
		}

		String root = context.getFilesDir().getAbsolutePath();
		if (directory.length() != 0) {
			root += "/" + directory;
		}

		filePath = root+"/"+fileName;
		
		try {
			FileOutputStream output = context.openFileOutput(filePath, flag);
			output.write(text.getBytes());
			output.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	
	public String readFile(boolean externalPreferred) {
		if (externalPreferred) {
			return readExternalFile();
		}
		return readInternalFile();
	}
	
	public String readInternalFile() {
		FileInputStream in;
		
		String root = context.getFilesDir().getAbsolutePath();
		if (directory.length() != 0) {
			root += "/" + directory;
		}
		
		try {
			in = context.openFileInput(root+"/"+fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return sb.toString();
	}

	public String readExternalFile(){
		if (externalFile == null) {
			if (getExternalFile() == false) {
				return "";
			}
		}

		try {			
			FileInputStream in = new FileInputStream(externalFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String dataRow = "";
			String buffer = "";
			while ((dataRow = reader.readLine()) != null) {
				buffer += dataRow + "\n";
			}
			//txtData.setText(aBuffer);
			reader.close();
			//Toast.makeText(getBaseContext(),		"Done reading SD 'mysdfile.txt'",		Toast.LENGTH_SHORT).show();
			return buffer;
		} catch (Exception e) {
			//Toast.makeText(getBaseContext(), e.getMessage(),	Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage());
		}
		return null;
	}



}
