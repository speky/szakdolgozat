package http.filehandler;

import java.io.File;
import java.util.HashSet;

public class HttpFileHandler {

	private static String TAG = "FileHandler: ";
	private Logger logger;
	private HashSet<FileInstance> fileSet;
	
	public HttpFileHandler(Logger logger) {
		this.logger = logger;
		fileSet = new  HashSet<FileInstance>();
	}
	
	public int getNumberOfFiles() {
		return fileSet.size();
	}
	
	public boolean isFileInSet(final String name) {
		for (FileInstance fi : fileSet){
			if (fi.getName().equals(name))                
				return true;
		}
		return false;
	}
	
	public boolean checkFileExistance(final String path){
		File file = new File(path);
		return file.exists();
	}
	
	public FileInstance addFile(final String fileName) {
		if (fileName == null || fileName.equals("")) {
			logger.addLine("fileName is empty!");
			return null;
		}
		if (isFileInSet(fileName)) {
			logger.addLine("fileName has already added!");
			return null;
		}
		
		if (!checkFileExistance(fileName)) {
			logger.addLine("fileName has not been existed!");
			return null;
		}
		
		logger.addLine(TAG+"reg new file: " + fileName);
		FileInstance file = new FileInstance(logger, fileName);
		fileSet.add(file);				
		return file;
	}
		
	public FileInstance getFileInstance(final String fileName) {
				
		for (FileInstance fi : fileSet){
			if (fi.getName().equals(fileName)){                
				return fi;
			}		
		}
		logger.addLine("File does not exist!, name: "+fileName);
		return null;
	}
}
