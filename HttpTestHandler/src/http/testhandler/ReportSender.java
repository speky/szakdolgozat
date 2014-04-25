package http.testhandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ReportSender implements MessageI{
	private final String TAG = "ReportSender: ";
	
	private Logger logger = null;	
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private int port = 0; 
	private PrintWriter printer = null;
		
	public ReportSender(Logger logger, int port){
		this.logger = logger;
		this.port = port;
		if (createSocket()) {
			try {
				printer = new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
				logger.addLine(TAG + e.getMessage());
			}
		}
	}

	public ReportSender(Logger logger, Socket socket){
		this.logger = logger;
		this.socket = socket;
		try {
			printer = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			logger.addLine(TAG + e.getMessage());
		}
	}
	
	public boolean isSocketConnected() {
		if (null != socket) {
			return socket.isConnected();
		}
		return false;
	}
	
	protected boolean createSocket() {
		try {			
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));			
			// set timer for the accept
			serverSocket.setSoTimeout(3000);
			
			socket = serverSocket.accept();
			
			serverSocket.setSoTimeout(0);

		} catch (Exception e) {
			logger.addLine(TAG + "Error in ReportSender socket:  "+e.getMessage());
			return false;
		}		
		return true;
	}

	public void stop() {
		try{
			if (socket != null) {
				socket.close();
			}
			if (null != serverSocket) {
				serverSocket.close();
			}
		}catch (Exception e) {
			logger.addLine(TAG + e.getMessage());
		}
		serverSocket = null;
		socket = null;
		logger = null;
	}
	
	@Override
	public void sendReportMessage(String id, String type,  String message){
	//	logger.addLine(TAG +"Send report message, id:"+ id);
		StringBuffer buffer = new StringBuffer();
		buffer.append("POST "+ id +" HTTP*/1.0\n");
		buffer.append("REPORT: "+ type +"\n");
		buffer.append("MESSAGE: "+message+"\n");
		buffer.append("END\n");
		printer.print(buffer);
		printer.flush();
	}
}
