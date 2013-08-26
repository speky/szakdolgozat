package http.filehandler;

import java.util.concurrent.Callable;

interface ICallback {	
	public void receiveReportMessages(int receivedBytes) ;
}

public class ConnectionInstance implements Callable<PacketStructure> {
	public static final int TCP_RECEIVER = 0;
	public static final int TCP_SENDER = 1;
	public static final int UDP_RECEIVER = 2;
	public static final int UDP_SENDER = 3;
	
	protected int id = -1;
	protected int type = -1;
	protected Logger logger = null;
	protected String errorMessage = null;
	protected PacketStructure packetStructure = null;
	
	public ConnectionInstance(final int type, final int id, Logger logger) {
		this.type = type;
		this.id = id;
		this.logger = logger;
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	final public int getPacketId(){
		if (packetStructure != null) {
			return packetStructure.id;
		}
		return -1;
	}
	
	final public int getReceivedPacket(){
		if (packetStructure != null) {
			return packetStructure.receivedPackets;
		}
		return 0;
	}
	
	/*final public int getSentPacket(){
		if (packetStructure != null) {
			return packetStructure.sentPackets;
		}
		return 0;
	}*/
	
	public String getErrorMessage() { 
		return errorMessage;
	}	
	
	public void stop() {}
	
	@Override
	public PacketStructure call() throws Exception {
		return null;
	}

}
