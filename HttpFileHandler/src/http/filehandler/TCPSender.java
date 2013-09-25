package http.filehandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Vector;


public class TCPSender  extends ConnectionInstance {	
		
	private byte[] byteBuffer;	
	private Socket socket = null;
	private boolean running = true;	
	private final String TAG = "TCPSender: ";	
	public static final String END_PACKET = "END_PACKET";	
	
	public TCPSender(Logger logger, final int id, final int bufferSize) {
		super(ConnectionInstance.TCP, id, logger);				
		logger.addLine(TAG+ "Created, id: " + id);		
		byteBuffer = new byte[bufferSize];
		Utility.fillStringBuffer(byteBuffer, bufferSize);		
	}

	public boolean setSocket(Socket socket) {		
		if (socket == null) {
			return false;
		}
		logger.addLine(TAG+"Id: " + id+ " address: " + socket.getInetAddress().getHostAddress()+ " port: " + socket.getPort());
		this.socket = socket;
		return true;
	}

	@Override
	public void stop() {
		logger.addLine(TAG+"Sending and report receiver stopped!");
				
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
		try {
			if (checkPrerequisite() == false) {
				return id;
			}
			OutputStream outputStream = socket.getOutputStream();
			logger.addLine(TAG+"Send message,  sendertId: " + id);
			while (running) {
				 outputStream.write(byteBuffer);
				 outputStream.flush();				 				 
			}			
			logger.addLine(TAG+" Sending ended!");
		} catch (Exception e) {
			errorMessage = "Error occured in sending packets";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
		}
		finally{
			try {								
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				errorMessage = "Socket cannot stopped!";
				logger.addLine(TAG+errorMessage);
				e.printStackTrace();
			}
		}
		return id;
	}

	private boolean checkPrerequisite() {
		if (socket == null) {
			logger.addLine(TAG+"Id: " + id+ " Connection problem!");			
			return false;
		}
		
		return true;
	}

	
}
