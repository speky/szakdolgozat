package http.filehandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
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
	private boolean running = true;
	private String errorMessage = null;
	private ICallback callback = null;
	private final String TAG = "TCPSender: ";	
	private final int ACK_WAITING = 5000; //in milisec	
	
	public static final String END_PACKET = "END_PACKET";	
	
	public TCPSender(Logger logger, final int id, ICallback callback) {
		super();
		this.callback = callback; 
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+ "Created, id: " + id);
		ackHandler = new AckHandler(logger);
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean setSocket(Socket socket) {		
		if (socket == null) {
			return false;
		}
		logger.addLine(TAG+"Iid: " + id+ " address: " + socket.getInetAddress().getHostAddress()+ " port: " + socket.getPort());
		this.socket = socket;
		return true;
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
			callback.setNumOfReceivedPackets(-1);
			callback.setNumOfSentPackets(-1);
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
				callback.setNumOfReceivedPackets(-1);
				callback.setNumOfSentPackets(-1);
				return -1;
			}
			ackHandler.startAckReceiver(fileInstance.getName(), socket, ackList);
			printerWriter = new PrintWriter(socket.getOutputStream());			
			logger.addLine(TAG+"Send message,  sendertId: " + id);			

			List<Packet> packetList = fileInstance.getPieces();
			int packetSize = packetList.size();
			for (int i = 0; i < packetSize && running; ++i) {
				Packet packet = packetList.get(i);
				sendMessage(fileInstance.getName(), packet.id, packet.hashCode, packet.text);
			}
			printerWriter.println("END\n");
			printerWriter.flush();
			logger.addLine(TAG+"File Sending ended!");
			int ackSize = ackList.size();
			if (ackSize < packetSize) {
				// wait for the last sent ACK message
				Thread.sleep(ACK_WAITING);
			}
			ackSize = ackList.size();
			ackHandler.stopScaning();
			logger.addLine(TAG+"Received ACK message: "+ackSize);			
			packetDiff = packetList.size()-ackSize;
			callback.setNumOfReceivedPackets(ackSize);
			callback.setNumOfSentPackets(packetList.size());
		} catch (Exception e) {
			errorMessage = "Error occured in sending packets";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			callback.setNumOfReceivedPackets(-1);
			callback.setNumOfSentPackets(-1);
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
		if (socket == null) {
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
		if (printerWriter == null) {
			return;
		}
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
