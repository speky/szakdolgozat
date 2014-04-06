package http.testhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


public class TCPSender  extends ConnectionInstance {	

	private byte[] byteBuffer;	
	private Socket socket = null;
	private boolean running = true;	
	private final String TAG = "TCPSender: ";

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
		logger.addLine(TAG+" stopped!");				
		running = false;

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
			socket.close();
			socket = null;
		}
		catch (IOException e) {
			errorMessage = "Socket cannot stopped!";
			logger.addLine(TAG+errorMessage);
			e.printStackTrace();
		}
		catch (Exception e) {
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
