package http.filehandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {

	private final String LOG_FILE_NAME = "log.txt";
	private File file = null;
	private FileWriter fileWriter = null;
	private String fileName = null;

	public String getFilePath() {
		return fileName;
	}
	private void makeFileInstance(final String filePath) {
		if (filePath.equals("")) {
			file = new File(LOG_FILE_NAME);
			fileName = LOG_FILE_NAME;
		}else{
			file = new File(filePath);
			fileName = filePath;
		}	
		try {
			file.createNewFile();
		} catch (IOException e) {
			System.out.println("Log file letrehozasi hiba: " + e.getMessage());
		}
	}

	private boolean makeFileWriter(){
		try {
			if (file != null && fileWriter == null){
				if (!file.canWrite()){
					if (!file.setWritable(true)){
						System.out.println("Log file nem irhato!");
						return false;
					}
				}
				fileWriter = new FileWriter(file);
				
				return true;
			}
		} catch (IOException e) {		
			System.out.println("Log writer kesztitesi hiba: " + e.getMessage());
		}
		return false;
	}
	
	public void closeFile(){
		if (fileWriter != null){
			try {
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Log writer lezárási hiba: " + e.getMessage());
			}
		}
	}
	public Logger(final String filePath){

		makeFileInstance(filePath);
		System.out.println("Log file: " + file.getAbsolutePath());
		if (makeFileWriter()){
			System.out.println("Log fileWriter created");			
		}else{
			System.out.println("Log fileWriter  created");			
		}
	}	
	public boolean addLine(final String logText){
		try{
			if (fileWriter != null){
				Date date = Calendar.getInstance().getTime();
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM); 
				fileWriter.append( dateFormat.format(date) +": "+ logText + "\n");
				fileWriter.flush();
				return true;
			}
		}catch(IOException e){
			System.out.println("Log hozzaadas hiba: " + e.getMessage());
		}
		return false;
	}

	public boolean deleteLogFile(){
		if (file != null){
			if (fileWriter != null){
				try {
					fileWriter.close();
				} catch (IOException e) {
					System.out.println("Log file lezaras hiba: " + e.getMessage());
				}
			}
			if (file.delete()){
				System.out.println("Log file torolve.");	
				return true;
			}	
		}else{
			System.out.println("Nincs log file! ");
		}
		return false;
	}

}
