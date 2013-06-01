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
	private ServerSocket serverSocket = null;
	private int serverPort = 0;
	private int receivedPackets = 0;
	private HttpParser parser = null;
	private AckHandler ackHandler = null;
	private boolean reading = true;
	private String errorMessage = null;
	
	private final String TAG = "TCPReceiver: "+id;
	private final int SOCKET_TIMEOUT = 1000; //in milisec
	
	public TCPReceiver(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+" TCP receiver created id: " + id);
		ackHandler = new AckHandler(logger);
		parser = new HttpParser(logger);
	}

	public String getErrorMEssage() {
		return errorMessage;
	}
	
	public void setSenderParameters(final int port) {
		serverPort = port;
		logger.addLine(TAG+" id: " + id+ " port: " + serverPort);
		serverSocket = createSocket();
	}

	protected ServerSocket createSocket() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(serverPort);
			socket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (Exception e) {
			errorMessage = "Server socket creation problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		} 
		return socket;
	}

	public int getReceivedPacket(){
		return receivedPackets;
	}
	
	public Integer call() {
		int packet = 0;
		try {			
			if (serverPort == 0) {
				errorMessage = "Invalid server port!";
				logger.addLine(TAG+errorMessage );				
				return -1;
			}

			Socket socket = serverSocket.accept();
			//figure out what is the ip-address of the client
			InetAddress client = socket.getInetAddress();
			logger.addLine(TAG+client + " connected to server.\n");
			socket.setSoTimeout(0);
			packet = readPackets(socket);
			socket.close();			
		}
		catch (SocketException e) {            
			errorMessage = "Socket closed while it is waiting for invoming connection.";
			logger.addLine(TAG+errorMessage);			
			packet = -1;
		}
		catch (SocketTimeoutException e) {            
			errorMessage = "Socket timed out, no connection received.";
			logger.addLine(TAG+"Error: "+errorMessage);
			packet = -1;
		}
		catch (Exception e) {
			 errorMessage = "Some kinf of error occured!";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			packet = -1;
		} 
		finally{
			try {
				if (!serverSocket.isClosed()) {
					logger.addLine(TAG+"Close server socket");
					serverSocket.close();
				}
			} catch (IOException e) {
				errorMessage = "Cannot close server socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}
		return packet;
	}

	public void stop() {
		logger.addLine(TAG+"Receiving stopped!");
		reading = false;
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				errorMessage = "Cannot close server socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}
	}
	
	public Integer readPackets(Socket socket) {
		BufferedReader reader = null;
		try {		
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			logger.addLine(TAG+"ERROR while create reader: " + e.getMessage());
			errorMessage = "cannot create BufferedReader";
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
							if (parser.getHeadProperty("ID") != null){
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
		if (parser.parseHttpMessage(message)==false) {
			errorMessage = "Problem occured while parsing message! message: " +message;
			logger.addLine(TAG+errorMessage);
			return false;
		}

		if (fileName== null) {
			fileName = parser.getMethodProperty("URI");
		}else if (!fileName.equals(parser.getMethodProperty("URI"))){
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
		
		if (parser.getHeadProperty("ID") != null){
			packetIds.add(Integer.parseInt(parser.getHeadProperty("ID")));
		}else{
			errorMessage = "Packet's id is invalid!";
			logger.addLine(TAG+errorMessage);
		}
		
		return true;
	}
}

