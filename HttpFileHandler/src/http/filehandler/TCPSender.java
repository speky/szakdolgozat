package http.filehandler;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

class TCPSender extends Thread {	
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private Socket socket = null;
	private PrintWriter printerWriter = null;
	private int serverPort = 0;
	private String serverAddress = "";

	public TCPSender(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine("TCP sender, id: " + id);
	}

	public void setReceiverParameters(final int port, final String address) {
		serverAddress = address;
		serverPort = port;
		logger.addLine("TCP sender, id: " + id+ " address: " + serverAddress + " port: " + serverPort);
	}

	public void setFile(final FileInstance instance) {
		fileInstance = instance;
		logger.addLine("TCP sender, id: " + id+ " fileName: " + instance.getName());
	}

	public void run() {
		try {			
			if (serverAddress == null || serverPort == 0) {
				logger.addLine("TCP sender, id: " + id+ " unknown receiver!");
				return;
			}

			if (fileInstance == null || fileInstance.getPocketSize() == 0) {
				logger.addLine("TCP sender, id: " + id+ " invalid file!");
				return;
			}

			socket = new Socket(serverAddress, serverPort);
			printerWriter = new PrintWriter(socket.getOutputStream());			
			logger.addLine("Send message,  sendertId: " + id);			


			Set<Packet> packetSet = fileInstance.getPieces();
			for (Packet packet : packetSet) {
				sendMessage(fileInstance.getName(), packet.id, packet.hashCode, packet.text);
			}
			sendMessage(fileInstance.getName(), 0, "0", "END");

		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage());
		} 
	}
	
	private void sendMessage(final String file, final int id, final String hash, final String message) {	
		StringBuffer buffer = new StringBuffer();
		buffer.append("POST "+ fileInstance.getName()+" HTTP*/1.0\n");
		buffer.append("ID: "+ id);
		buffer.append("HASH: "+ hash);
		buffer.append("TEXT: "+ message);
		printerWriter.println(buffer);
		printerWriter.flush();
	}
}

class TCPReceiver extends Thread {	
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private ServerSocket serverSocket = null;
	private int serverPort = 0;	

	public TCPReceiver(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine("TCP receiver, id: " + id);
	}

	public void setSenderParameters(final int port) {

		serverPort = port;
		logger.addLine("TCP sender, id: " + id+ " port: " + serverPort);
	}

	public void run() {
		try {			
			if (serverPort == 0) {
				logger.addLine("TCP sender, id: " + id+ " unknown receiver!");
				return;
			}

			if (fileInstance == null || fileInstance.getPocketSize() == 0) {
				logger.addLine("TCP sender, id: " + id+ " invalid file!");
				return;
			}

			serverSocket = new ServerSocket(serverPort);
			Socket socket = serverSocket.accept();
			//figure out what is the ip-address of the client
			InetAddress client = socket.getInetAddress();
			//and print it to log
			logger.addLine(client + " connected to server.\n");			
			Scanner scanner = new Scanner(socket.getInputStream());
			StringBuffer buffer = new StringBuffer();

			while (scanner.hasNextLine()) {			
				String readedLine = scanner.nextLine();
				buffer.append(readedLine);
				while (readedLine.compareTo("END") !=  0) {
					readedLine = scanner.nextLine();
					if (readedLine.compareTo("END") !=  0) {
						buffer.append("+"+readedLine);
					}
					logger.addLine("Send message,  sendertId: " + id);
				}
			}
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage());
		} 

	}
