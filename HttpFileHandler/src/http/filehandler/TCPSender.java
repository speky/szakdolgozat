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
	private PrintWriter printerWriter = null;
	private MessageI reportSender = null;
	private ReceiverReportI reportReceiver = null;
	private Vector<Integer> reportList = new Vector<Integer>();
	private boolean running = true;	
	private final String TAG = "TCPSender: ";	
	private final int ACK_WAITING = 5000; //in milisec
	public static final String END_PACKET = "END_PACKET";	
	
	public TCPSender(Logger logger, final int id, final int bufferSize, MessageI sender, ReceiverReportI receiver) {
		super(ConnectionInstance.TCP, id, logger);				
		logger.addLine(TAG+ "Created, id: " + id);
		reportSender = sender;
		reportReceiver = receiver;
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
				return -1;
			}
			OutputStream outputStream = socket.getOutputStream();
			logger.addLine(TAG+"Send message,  sendertId: " + id);
			while (running) {
				 outputStream.write(byteBuffer);
				 outputStream.flush();
				 if (null != reportReceiver) {
					 reportReceiver.setSentBytes(byteBuffer.length);
				 }else if (null != reportSender) {
					 reportSender.sendReportMessage(Integer.toString(id), "TCP", "message");
				 }
				 
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
		return 0;
	}

	private boolean checkPrerequisite() {
		if (socket == null) {
			logger.addLine(TAG+"Id: " + id+ " Connection problem!");			
			return false;
		}
		
		return true;
	}

	
}
