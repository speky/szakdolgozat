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

public class TCPReceiver implements Callable<Integer> {
	private Logger logger = null;	
	private int id = 0;
	private HashSet<Integer> packetIds = new HashSet<Integer>();
	private String fileName = null;
	private int receivedPackets = 0;
	private HttpParser parser = null;
	private AckHandler ackHandler = null;
	private boolean reading = true;
	private String errorMessage = null;
	private ICallback callback = null;	
	private final String TAG = "TCPReceiver: "+id;
	private Socket socket = null;	
	
	public TCPReceiver(Logger logger, final int id, ICallback callback) {
		super();
		this.callback = callback;
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+" TCP receiver created id: " + id);
		ackHandler = new AckHandler(logger);
		parser = new HttpParser(logger);
	}

	public String getErrorMEssage() {
		return errorMessage;
	}
	
	public void setSocket(Socket socket) {
		this.socket  = socket;
	}

	public int getReceivedPacket(){
		return receivedPackets;
	}
	
	public Integer call() {
		int packet = 0;
		try {			
			if (socket == null) {
				errorMessage = "Invalid socket!";
				logger.addLine(TAG+errorMessage );
				callback.setNumOfReceivedPackets(-1);
				callback.setNumOfSentPackets(-1);
				return -1;
			}						
			packet = readPackets();
			socket.close();			
		}		
		catch (Exception e) {
			 errorMessage = "Some kinf of error occured!";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			callback.setNumOfReceivedPackets(-1);
			callback.setNumOfSentPackets(-1);
			packet = -1;
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
		callback.setNumOfReceivedPackets(packet);
		callback.setNumOfSentPackets(packet);
		return packet;
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
	
	public Integer readPackets() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			logger.addLine(TAG+"ERROR while create reader: " + e.getMessage());
			errorMessage = "cannot create BufferedReader";
			return 0;
		}		
		StringBuffer buffer = new StringBuffer();
		receivedPackets = 0;
		String readedLine = null;
		try {
			while (reading && (readedLine = reader.readLine()) != null) {							
				buffer.append(readedLine+"+");
				if (readedLine.compareTo(TCPSender.END_PACKET) ==  0) {
					if (makePacket(buffer.toString())) {
						logger.addLine(TAG+"Create packet, id: " + receivedPackets);					
						try {
							if (parser.getHeadProperty("ID") != null) {
								int id = Integer.parseInt(parser.getHeadProperty("ID"));
								ackHandler.sendAckMessage(socket.getOutputStream(), fileName, id);
							}
						}catch (IOException e) {
							logger.addLine(TAG+"Error: ack handler received invalid Id!");
							e.printStackTrace();
						}
						++receivedPackets;
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
		return receivedPackets;
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

