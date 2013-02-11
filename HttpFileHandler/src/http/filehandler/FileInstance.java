package http.filehandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileInstance {

	private String fileName;
	private String hashId;
	private int count;
	private Set<Packet> pieces;

	private Logger logger;
	private final String TAG = "FileInstance: "; 
	public static final int DEFAULT_SIZE = 10; 

	public FileInstance(Logger logger, final String name) {
		this.logger = logger;
		this.fileName = name;
		hashId = "";
		count = 0;
		pieces = new HashSet<Packet> ();
	}

	public String getName() {
		return fileName;
	}

	public String getCheckSum() {		
		return hashId;
	}
	
	public int getPocketSize() {
		return pieces.size();
	}

	public boolean SplitFileToPockets(int packetSize) {
		File file = new File(fileName);
		
		if (file.canRead()){								    
			// Create the byte array to hold the data
			int length = (int) file.length();
			byte[] bytes = new byte[length];
			if (ReadWholeFile(bytes, file) == false) {
				logger.addLine(TAG+"problem occured while reading a file, file name:"+fileName);
				return false;
			}
			hashId = Utility.calcCheckSum(bytes);
			if (PocketizeTheFile(bytes, packetSize, length) == false){				
				return false;
			}			
		}else {
			logger.addLine(TAG+"Could not read the file, file name:"+fileName);			
		}
		return false;
	}

	private boolean PocketizeTheFile(byte[] bytes, int packetSize, final int length) {
		try{				
			int begin = 0;
			while (begin < length) {
				if (begin+packetSize >= length) {
					packetSize = length - begin;
				}					
				AddPiece(count++,  new String(Arrays.copyOfRange(bytes, begin, begin+packetSize)));
				begin += packetSize;
			}
			return true;
		}catch (Exception ex) {
			logger.addLine(TAG+"problem occured while create packet, file name:"+fileName);
			logger.addLine(TAG+ex.getMessage());
		}
		return false;
	}
	private boolean ReadWholeFile(byte[] bytes, final File inputFile) {
		try{ 
			// Read in the bytes
			InputStream is = new FileInputStream(inputFile);
			int offset = 0;
			int numRead = 0;
			int length = (int)inputFile.length();
			while (offset < length) {
				numRead = is.read(bytes, offset, length-offset);
				offset += numRead;		            
			}
			// Close the input stream and return bytes
			is.close();
			return true;
		}catch (Exception ex) {
			ex.printStackTrace();
		}	
		return false;
	}

	private void AddPiece(int id, String text) {
		for (Packet p : pieces) {
			if (p.id == id) {
				logger.addLine("pocket id is already in use!, id:" + id);
				return;
			}
		}		
		logger.addLine("New packet added, id: "+id);
		Packet p = new Packet(text, text.length(), id);
		pieces.add(p);
	}



}
