package http.filehandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;


class TCPReceiver implements Callable {
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
			//and print it to log
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
			// 
			e.printStackTrace();
		}
		StringBuffer buffer = new StringBuffer();
		int receivedPacket = 0;
		boolean reading = true;
		while (reading && scanner.hasNextLine()) {							
			String readedLine = scanner.nextLine();
			buffer.append(readedLine);
			if (readedLine.compareTo("END_PACKET") ==  0) {
				makePacket(buffer.toString());
				buffer.delete(0, buffer.length());
				receivedPacket++;
			}else if (readedLine.compareTo("END") ==  0) {
				reading = false;
			}
			logger.addLine("Send message,  sendertId: " + id);
		}
		return receivedPacket;
	}

	private void makePacket(String string) {


	}
}

