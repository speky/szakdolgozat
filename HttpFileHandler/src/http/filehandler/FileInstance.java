package http.filehandler;

import java.util.HashSet;
import java.util.Set;

public class FileInstance {

	public String name;
	public  String hashId;
	public  int count;
	public Set<Packet> pieces;

	private Logger logger;
	
	public FileInstance(Logger logger) {
		this.logger = logger;
		name = "";
		hashId = "";
		count = 0;
		pieces = new HashSet<Packet> ();
	}

	public FileInstance(Logger logger, final String name) {
		this.logger = logger;
		this.name = name;
		this.hashId = "";
		this.count = count;
		pieces = new HashSet<Packet> ();
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
