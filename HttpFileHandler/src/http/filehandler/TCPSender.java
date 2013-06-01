package http.filehandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

public class TCPSender  implements Callable<Integer> {	
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private Socket socket = null;
	private PrintWriter printerWriter = null;
	private AckHandler ackHandler = null;
	private HashSet<Integer> ackList = new HashSet<Integer>();
	private int serverPort = 0;
	private String serverAddress = "";
	private boolean running = true;
	private String errorMessage = null;
	private final String TAG = "TCPSender: ";	
	private final int SOCKET_TIMEOUT = 300; //in milisec
	private final int REPEAT_SOCKET_CONNECTION= 3;
	private final int ACK_WAITING = 5000; //in milisec	
	
	public static final String END_PACKET = "END_PACKET";	
	
	public TCPSender(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+ "Created, id: " + id);
		ackHandler = new AckHandler(logger);
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean setReceiverParameters(final int port, final String address) {
		serverAddress = address;
		serverPort = port;
		logger.addLine(TAG+"Iid: " + id+ " address: " + serverAddress + " port: " + serverPort);
		int repeatId = 0;
		do {
		 socket = createSocket();
		 ++repeatId;
		}while (socket== null && REPEAT_SOCKET_CONNECTION > repeatId);
		
		if (socket == null){
			return false;
		}		
		return true;
	}

	protected Socket createSocket() {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(serverAddress, serverPort), SOCKET_TIMEOUT);
			logger.addLine(TAG+" Create new socket");
			return socket;
		} catch (UnknownHostException e) {
			errorMessage = "Socket creatin problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
		} catch (IOException e) {
			errorMessage = "Socket creatin problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		}
		return null;
	}

	public void setFile(final FileInstance instance) {
		fileInstance = instance;
		logger.addLine(TAG+"Id: " + id+ " fileName: " + instance.getName());
	}

	public void stop() {
		logger.addLine(TAG+"Sending and ack receiver stopped!");
		ackHandler.stopScaning();
		running = false;
		try {
			socket.close();
		} catch (IOException e) {
			errorMessage = "Socket cannot stopped!";
			logger.addLine(TAG+errorMessage);
			e.printStackTrace();
		}
	}
	
	public Integer call() {
		int packetDiff= 0;
		try {			
			if (checkPrerequisite() == false) {					
				return -1;
			}
			ackHandler.startAckReceiver(fileInstance.getName(), socket, ackList);
			printerWriter = new PrintWriter(socket.getOutputStream());			
			logger.addLine(TAG+"Send message,  sendertId: " + id);			

			List<Packet> packetList = fileInstance.getPieces();
			int packetSize = packetList.size();
			for (int i=0; i < packetSize && running; ++i) {
				Packet packet = packetList.get(i);
				sendMessage(fileInstance.getName(), packet.id, packet.hashCode, packet.text);
			}
			printerWriter.println("END\n");
			printerWriter.flush();
			logger.addLine(TAG+"File Sending ended!");
			// wait for the last sent ACK message
			Thread.sleep(ACK_WAITING);
			ackHandler.stopScaning();
			int ackSize = ackList.size();
			logger.addLine(TAG+"Received ACK message: "+ackSize);			
			packetDiff = packetList.size()-ackSize;
		} catch (Exception e) {
			errorMessage = "Error occured in sending packets";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			packetDiff = -1;
		} 
		finally{
			try {
				if (socket != null) {
					socket.close();
				}
				ackHandler.stopScaning();
			} catch (IOException e) {
				errorMessage = "Socket cannot stopped!";
				logger.addLine(TAG+errorMessage);
				e.printStackTrace();
			}
		}
		return packetDiff;
	}

	private boolean checkPrerequisite() {
		if (serverAddress == null || serverPort == 0 || socket == null) {
			logger.addLine(TAG+"Id: " + id+ " Connection problem!");			
			return false;
		}

		if (fileInstance == null || fileInstance.getPocketSize() == 0) {
			logger.addLine(TAG+"Id: " + id+ " invalid file!");			
			return false;
		}
		return true;
	}

	private void sendMessage(final String file, final int id, final String hash, final String message) {	
		StringBuffer buffer = new StringBuffer();
		buffer.append("POST "+ fileInstance.getName()+" HTTP*/1.0\n");
		buffer.append("ID: "+ id+"\n");
		buffer.append("HASH: "+ hash +"\n");
		buffer.append("TEXT: "+ message +"\n");
		buffer.append(END_PACKET);
		printerWriter.println(buffer);
		printerWriter.flush();
	}
}
