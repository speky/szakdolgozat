package http.filehandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;

import javax.rmi.CORBA.Util;


class TCPReceiver implements Callable {
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private ServerSocket serverSocket = null;
	private int serverPort = 0;
	private HttpParser parser = null;

	public TCPReceiver(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		logger.addLine("TCP receiver, id: " + id);
		parser = new HttpParser(logger);
	}

	public void setSenderParameters(final int port) {
		serverPort = port;
		logger.addLine("TCP sender, id: " + id+ " port: " + serverPort);
		serverSocket = createSocket();
	}

	protected ServerSocket createSocket() {
		ServerSocket socket;
		try {
			socket = new ServerSocket(serverPort);
			return socket;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Integer call() {
		try {			
			if (serverPort == 0) {
				logger.addLine("TCP sender, id: " + id+ " unknown receiver!");
				return 0;
			}

			if (fileInstance == null || fileInstance.getPocketSize() == 0) {
				logger.addLine("TCP sender, id: " + id+ " invalid file!");
				return 0;
			}

			Socket socket = serverSocket.accept();
			//figure out what is the ip-address of the client
			InetAddress client = socket.getInetAddress();
			logger.addLine(client + " connected to server.\n");			
			return readPackets(socket);
			
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage());
		} 
		return 0;
	}

	public Integer readPackets(Socket socket) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			logger.addLine("ERROR while create scanner: " + e.getMessage());
		}
		StringBuffer buffer = new StringBuffer();
		int receivedPacket = 0;
		boolean reading = true;
		while (reading && scanner.hasNextLine()) {							
			String readedLine = scanner.nextLine();
			buffer.append(readedLine+"+");
			if (readedLine.compareTo(TCPSender.END_PACKET) ==  0) {
				if (makePacket(buffer.toString())){
					logger.addLine("Create packet, id: " + receivedPacket);
					receivedPacket++;
				}
				buffer.delete(0, buffer.length());
			}else if (readedLine.compareTo("END") ==  0) {
				reading = false;
				logger.addLine("End message received");
			}			
		}
		return receivedPacket;
	}

	private boolean makePacket(String message) {
		if (parser.parseHttpMessage(message)==false){
			logger.addLine("Problem occured while parsing message! message: " +message);
			return false;
		}

		if (fileInstance == null) {
			fileInstance = new FileInstance(logger, parser.getMethodProperty("URI"));
		}
		
		String text = parser.getHeadProperty("TEXT");
		String calcedHash = Utility.calcCheckSum(text.getBytes());
		if (parser.getHeadProperty("HASH") == null || !parser.getHeadProperty("HASH").equals(calcedHash)) {
			return false;
		}
		
		if (fileInstance.addPacket(Integer.parseInt(parser.getHeadProperty("ID")), text) == false) {
			logger.addLine("Problem occured while add new package!");
			return false;
		}		
		return true;
	}
}

