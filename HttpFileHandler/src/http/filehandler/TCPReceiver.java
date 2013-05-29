package http.filehandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;


public class TCPReceiver implements Callable<Integer> {
	private Logger logger = null;	
	private int id = 0;
	private List<PacketDescriptor> packetList = new ArrayList<PacketDescriptor>();
	private String fileName = null;
	private ServerSocket serverSocket = null;
	private int serverPort = 0;
	private int receivedPackets = 0;
	private HttpParser parser = null;
	private AckHandler ackHandler = null;
	private boolean reading = true;
	private final String TAG = "TCPReceiver: ";
	
	public TCPReceiver(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+" id: " + id);
		ackHandler = new AckHandler(logger);
		parser = new HttpParser(logger);
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {            
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		} 
		return socket;
	}

	public int getReceivedPacket(){
		return receivedPackets;
	}
	
	public Integer call() {
		try {			
			if (serverPort == 0) {
				logger.addLine(TAG+"Invalid server port!");
				return -1;
			}

			Socket socket = serverSocket.accept();
			//figure out what is the ip-address of the client
			InetAddress client = socket.getInetAddress();
			logger.addLine(TAG+client + " connected to server.\n");			
			return readPackets(socket);

		} catch (Exception e) {            
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		} 
		return -1;
	}

	public void stop() {
		logger.addLine(TAG+"Receiving stopped!");
		reading = false;
	}
	
	public Integer readPackets(Socket socket) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			logger.addLine(TAG+"ERROR while create scanner: " + e.getMessage());
		}
		
		StringBuffer buffer = new StringBuffer();
		receivedPackets = 0;		
		while (reading && scanner.hasNextLine()) {							
			String readedLine = scanner.nextLine();			
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
			}else if (readedLine.compareTo("END") ==  0) {
				reading = false;
				logger.addLine(TAG+"End message received");
			}			
		}
		scanner.close();
		return receivedPackets;
	}

	private boolean makePacket(String message) {
		if (parser.parseHttpMessage(message)==false) {
			logger.addLine(TAG+"Problem occured while parsing message! message: " +message);
			return false;
		}

		if (fileName== null) {
			fileName = parser.getMethodProperty("URI");
		}else if (!fileName.equals(parser.getMethodProperty("URI"))){
			logger.addLine(TAG+"Packet what received is belong to another file! fileName: "+parser.getMethodProperty("URI"));
			return false; 
		}

		String text = parser.getHeadProperty("TEXT");
		String calcedHash = Utility.calcCheckSum(text.getBytes());
		String origHash = null;
		if (parser.getHeadProperty("HASH") != null ) {
			origHash = parser.getHeadProperty("HASH");		
			if (!origHash .equals(calcedHash)) {
		
				logger.addLine(TAG+"hash is invalid! calced: "+calcedHash + " original:"+origHash);
				return false;
			}
		}else{
			logger.addLine(TAG+"original hash is invalid!");
			return false;
		}

		/*PacketDescriptor packet = new PacketDescriptor();
		packet.fileName = fileName;
		packet.packetId = Integer.parseInt(parser.getHeadProperty("ID"));
		packet.hashId = calcedHash;
		packet.origHahId = origHash;
		
		packetList.add(packet);
			*/
		return true;
	}
}

class PacketDescriptor {	
	public  int packetId;
	public  String hashId;
	public String origHahId;	
	public String fileName;	
		
}

