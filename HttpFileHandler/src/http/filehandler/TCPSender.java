package http.filehandler;

import java.io.IOException;
import java.io.PrintWriter;
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
	
	public static final String END_PACKET = "END_PACKET";
	
	public TCPSender(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine("TCP sender, id: " + id);
		ackHandler = new AckHandler(logger);
	}

	public void setReceiverParameters(final int port, final String address) {
		serverAddress = address;
		serverPort = port;
		logger.addLine("TCP sender, id: " + id+ " address: " + serverAddress + " port: " + serverPort);
		socket = createSocket();
	}

	protected Socket createSocket() {
		try {
			Socket socket = new Socket(serverAddress, serverPort);
			logger.addLine("TCP Sender, Create new socket");
			return socket;
		} catch (UnknownHostException e) {
			logger.addLine("ERROR in run() " + e.getMessage());			
		} catch (IOException e) {
			logger.addLine("ERROR in run() " + e.getMessage());
		}
		return null;
	}

	public void setFile(final FileInstance instance) {
		fileInstance = instance;
		logger.addLine("TCP sender, id: " + id+ " fileName: " + instance.getName());
	}

	public Integer call() {
		try {			
			if (checkPrerequisite() == false) {					
				return -1;
			}
			ackHandler.startAckReceiver(fileInstance.getName(), socket, ackList);
			printerWriter = new PrintWriter(socket.getOutputStream());			
			logger.addLine("Send message,  sendertId: " + id);			

			List<Packet> packetList = fileInstance.getPieces();
			for (Packet packet : packetList) {
				sendMessage(fileInstance.getName(), packet.id, packet.hashCode, packet.text);
			}
			printerWriter.println("END");
			printerWriter.flush();
			System.out.println("File Sending ended!");
			Thread.sleep(1000);
			ackHandler.stopScaning();
			int ackSize = ackList.size();
			logger.addLine("Received ACK message: "+ackSize);
			System.out.println("Received ACK message: "+ackSize);
			
			return packetList.size()-ackSize;
			
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage());
		} 
		return -1;
	}

	private boolean checkPrerequisite() {
		if (serverAddress == null || serverPort == 0 || socket == null) {
			logger.addLine("TCP sender, id: " + id+ " unknown receiver!");
			System.out.println("connection problem!");
			return false;
		}

		if (fileInstance == null || fileInstance.getPocketSize() == 0) {
			logger.addLine("TCP sender, id: " + id+ " invalid file!");
			System.out.println("Problem with fileInstance!");
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
