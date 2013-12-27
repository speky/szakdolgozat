
package http.testhandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TCPReceiver extends ConnectionInstance {	
	private boolean reading = true;			
	private final String TAG = "TCPReceiver: "+ id;
	private Socket socket = null;
	private int reportInterval = 0;
	private Timer timer = null;
	private int readedBytes = 0;
	private MessageI reportSender = null;
	private ReceiverReportI reportReceiver = null;
	
	public TCPReceiver(Logger logger, final int id, MessageI sender, ReceiverReportI receiver) {
		super(ConnectionInstance.TCP, id, logger);
		logger.addLine(TAG + " TCP receiver created id: " + Integer.toString(id));
		reportSender = sender;
		reportReceiver = receiver;
	}
		
	public void setReportInterval(int milisec) {
		reportInterval = milisec;
	}
	
	public boolean setSocket(Socket socket) {
		if (socket == null) {
			return false;
		}
		logger.addLine(TAG+"Id: " + id+ " address: " + socket.getInetAddress().getHostAddress()+ " port: " + socket.getPort());
		this.socket = socket;
		return true;
	}
		
	public Integer call() {	
		try {
			if (socket == null) {
				errorMessage = "Invalid socket!";
				logger.addLine(TAG+errorMessage );
				return id;
			}
			
			if (reportInterval > 0) {
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					  @Override
					  public void run() {
						  if (reportReceiver  != null ) {								
							  reportReceiver.setReceivedBytes(id, reportInterval, readedBytes);
						  } else if (null != reportSender){
							  reportSender.sendReportMessage(Integer.toString(id), "TCP", Integer.toString(readedBytes));
						  }
						  readedBytes = 0;
					  }
					}, reportInterval, reportInterval);
			}
			
			readPackets();
			socket.close();
		}		
		catch (Exception e) {
			 errorMessage = "Some kinf of error occured!";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		} 
		finally{
			if (timer != null) {
				timer.cancel();
			}
			try {
				if (!socket.isClosed()) {
					logger.addLine(TAG+"Close socket");
					socket.close();
				}				
				
			} catch (IOException e) {
				errorMessage = "Cannot close socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}		
		return id;
	}

	@Override
	public void stop() {
		logger.addLine(TAG+" stopped!");
		reading = false;
		if (timer != null) {
			timer.cancel();
		}		
		if (socket != null) {
			try {				
				socket.close();				
			} catch (IOException e) {
				errorMessage = "Cannot close socket!";
				logger.addLine(TAG+ errorMessage);
				e.printStackTrace();
			}
		}
	}
	
	public void readPackets() {
		InputStream inputStream = null;
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			logger.addLine(TAG + e.getMessage());			
			e.printStackTrace();
		}
		int size = 2000;
		byte[] byteBuffer = new byte[size];
						
		//socket.setReceiveBufferSize(1024);//1KB
		//logger.addLine(TAG + "rec buff size: "+ socket.getReceiveBufferSize());
				
		try {
			while (reading && inputStream != null) {
				readedBytes += inputStream.read(byteBuffer);		
				//logger.addLine(TAG+"Readed Bytes: "+readedBytes);								
			}					
		} catch (IOException e) {
			logger.addLine(TAG+e.getMessage());			
			e.printStackTrace();			
		}
		finally {
			byteBuffer = null;
			if (inputStream != null) {
				try {					
					inputStream.close();
				} catch (IOException e) {
					logger.addLine(TAG+e.getMessage());
					e.printStackTrace();
				}
			}
		}		
	}
}

