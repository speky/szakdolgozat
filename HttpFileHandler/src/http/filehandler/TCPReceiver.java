package http.filehandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class TCPReceiver implements Callable<PacketStructure>{
	private Logger logger = null;	
	private int id = 0;
	private HashSet<Integer> packetIds = new HashSet<Integer>();
	private String fileName = null;	
	private HttpParser parser = null;
	private AckHandler ackHandler = null;
	private boolean reading = true;
	private String errorMessage = null;		
	private final String TAG = "TCPReceiver: "+ id;
	private Socket socket = null;
	private PacketStructure packetStrucutre;
	
	public TCPReceiver(Logger logger, int id) {
		super();		
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+" TCP receiver created id: " + id);
		ackHandler = new AckHandler(logger);
		parser = new HttpParser(logger);
		packetStrucutre = new PacketStructure();
	}

	final public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setSocket(Socket socket) {
		this.socket  = socket;
	}

	final public int getSentPacket(){
		return packetStrucutre.sentPackets;
	}
	
	final public int getReceivedPacket(){
		return packetStrucutre.receivedPackets;
	}
	
	public PacketStructure call() {		
		try {			
			if (socket == null) {
				errorMessage = "Invalid socket!";
				logger.addLine(TAG+errorMessage );
				packetStrucutre.receivedPackets = -1;
				return packetStrucutre;
			}						
			readPackets();
			socket.close();
		}		
		catch (Exception e) {
			 errorMessage = "Some kinf of error occured!";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
			packetStrucutre.receivedPackets = -1;
			return packetStrucutre;
		} 
		finally{
			try {
				if (!socket.isClosed()) {
					logger.addLine(TAG+"Close socket");
					socket.close();
				}
			} catch (IOException e) {
				errorMessage = "Cannot close socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}		
		return packetStrucutre;
	}

	public void stop() {
		logger.addLine(TAG+"Receiving stopped!");
		reading = false;
		if (socket != null) {
			try {				
				socket.close();				
			} catch (IOException e) {
				errorMessage = "Cannot close socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}
	}
	
	public void readPackets() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			errorMessage = "cannot create BufferedReader";
			logger.addLine(TAG+"ERROR while create reader: " + errorMessage );			
			packetStrucutre.receivedPackets = -1;
			return;
		}		
		StringBuffer buffer = new StringBuffer();
		packetStrucutre.receivedPackets = 0;
		packetStrucutre.sentPackets = 0;
		String readedLine = null;
		try {
			while (reading && (readedLine = reader.readLine()) != null) {							
				buffer.append(readedLine+"+");
				if (readedLine.compareTo(TCPSender.END_PACKET) ==  0) {
					if (makePacket(buffer.toString())) {
						logger.addLine(TAG+"Create packet, id: " + packetStrucutre.receivedPackets);					
						try {
							if (parser.getHeadProperty("ID") != null) {
								int id = Integer.parseInt(parser.getHeadProperty("ID"));
								if (ackHandler  != null ) {
									ackHandler.sendAckMessage(socket.getOutputStream(), fileName, id);
									++packetStrucutre.sentPackets;
								}
							}
						}catch (IOException e) {
							logger.addLine(TAG+"Error: ack handler received invalid Id!");
							e.printStackTrace();
						}
						++packetStrucutre.receivedPackets;
					}
					buffer.delete(0, buffer.length());
					readedLine = null;
				}else if (readedLine.compareTo("END") ==  0) {
					reading = false;
					logger.addLine(TAG+"End message received");
				}
			}
		} catch (NumberFormatException e) {
			logger.addLine(TAG+e.getMessage());			
			e.printStackTrace();			
		} catch (IOException e) {
			logger.addLine(TAG+e.getMessage());			
			e.printStackTrace();			
		}
		finally {			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.addLine(TAG+e.getMessage());
					e.printStackTrace();
				}
			}
		}		
	}

	private boolean makePacket(String message) {
		if (parser.parseHttpMessage(message) == false) {
			errorMessage = "Problem occured while parsing message! message: " +message;
			logger.addLine(TAG+errorMessage);
			return false;
		}

		if (fileName== null) {
			fileName = parser.getMethodProperty("URI");
		}else if (!fileName.equals(parser.getMethodProperty("URI"))) {
			errorMessage = "Packet what received is belong to another file! fileName: "+parser.getMethodProperty("URI");
			logger.addLine(TAG+errorMessage);
			return false; 
		}

		String text = parser.getHeadProperty("TEXT");
		String calcedHash = Utility.calcCheckSum(text.getBytes());
		String origHash = null;
		if (parser.getHeadProperty("HASH") != null ) {
			origHash = parser.getHeadProperty("HASH");		
			if (!origHash .equals(calcedHash)) {
				errorMessage = "hash is invalid! calced: "+calcedHash + " original:"+origHash;
				logger.addLine(TAG+errorMessage);
				return false;
			}
		}else{
			errorMessage = "Original hash is invalid!";
			logger.addLine(TAG+errorMessage);			
		}
		
		if (parser.getHeadProperty("ID") != null) {
			packetIds.add(Integer.parseInt(parser.getHeadProperty("ID")));
		}else{
			errorMessage = "Packet's id is invalid!";
			logger.addLine(TAG+errorMessage);
		}
		
		return true;
	}
}

