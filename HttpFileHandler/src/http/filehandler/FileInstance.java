package http.filehandler;

import java.util.HashSet;
import java.util.Set;

public class FileInstance {

	private String fileName;
	private String hashId;
	private int count;
	private Set<Packet> pieces;

	private Logger logger;
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
	public boolean SplitFileToPockets(final int packetSize) {
		return true;
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
