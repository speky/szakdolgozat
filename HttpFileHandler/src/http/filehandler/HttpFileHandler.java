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
	
	public boolean IsFileInSet(final String name) {
		for (FileInstance fi : fileSet){
			if (fi.getName().equals(name))                
				return true;
		}
		return false;
	}
	
	public boolean CheckFileExistance(final String path){
		File file = new File(path);
		return file.exists();
	}
	
	public FileInstance AddFile(final String fileName) {
		if (fileName == null || fileName.equals("")) {
			logger.addLine("fileName is empty!");
			return null;
		}
		if (IsFileInSet(fileName)) {
			logger.addLine("fileName has already added!");
			return null;
		}
		
		if (!CheckFileExistance(fileName)) {
			logger.addLine("fileName has not been existed!");
			return null;
		}
		
		logger.addLine(TAG+"reg new file: " + fileName);
		FileInstance file = new FileInstance(logger, fileName);
		fileSet.add(file);
		file.SplitFileToPockets(FileInstance.DEFAULT_SIZE);		
		return file;
	}

}
