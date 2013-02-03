package http.filehandler;

import java.util.HashSet;

public class HttpFileHandler {

	private static String TAG = "FileHandler: ";
	private Logger logger;
	private HashSet<FileInstance> fileSet;
	
	public HttpFileHandler(Logger logger) {
		this.logger = logger;
		fileSet = new  HashSet<FileInstance>();
	}
	
	public boolean isFileInSet(final String name) {
		for (FileInstance fi : fileSet){
			if (fi.name.equals(name))                
				return true;
		}
		return false;
	}
	
	public FileInstance AddFile(final String fileName) {
		if (fileName == null){
			logger.addLine("fileName is empty!");
			return null;
		}
		for (FileInstance fi : fileSet){
			if (fi.name.equals(fileName))                
				logger.addLine("File has already added: " + fileName);
			return fi;
		}
		logger.addLine(TAG+"reg new file: " + fileName);
		FileInstance file = new FileInstance(logger, fileName);
		file.SplitFileToPockets(FileInstance.DEFAULT_SIZE);
		fileSet.add(file);
		return file;
	}

}
