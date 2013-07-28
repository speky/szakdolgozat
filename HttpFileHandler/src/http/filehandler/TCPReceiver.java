
package http.filehandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class TCPReceiver implements Callable<PacketStructure>{
	private Logger logger = null;	
	private String id = "0";		
	private ReportHandler reportHandler = null;
	private boolean reading = true;
	private String errorMessage = null;		
	private final String TAG = "TCPReceiver: "+ id;
	private Socket socket = null;
	private PacketStructure packetStrucutre;
	private int reportInterval = 0;
	private Timer timer = null;
	int totalReadedBytes = 0;
	
	public TCPReceiver(Logger logger, String id) {
		super();		
		this.id = id;
		this.logger = logger;
		logger.addLine(TAG+" TCP receiver created id: " + id);
		reportHandler = new ReportHandler(logger);		
		packetStrucutre = new PacketStructure();
	}

	final public String getErrorMessage() {
		return errorMessage;
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

	final public int getSentPacket(){
		return packetStrucutre.sentPackets;
	}
	
	final public int getReceivedPacket(){
		return packetStrucutre.receivedPackets;
	}
	
	public PacketStructure call() {		
		try {			
			if (socket == null) {
				errorMessage = "Invalid socket!";
				logger.addLine(TAG+errorMessage );
				packetStrucutre.receivedPackets = -1;
				return packetStrucutre;
			}
			
			if (reportInterval > 0) {
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					  @Override
					  public void run() {
						  if (reportHandler  != null ) {
								try {
									reportHandler.sendReportMessage(socket.getOutputStream(), id, totalReadedBytes);
								} catch (IOException e) {
									logger.addLine(TAG+ "send report problem");
									e.printStackTrace();
								}
						  }
					  }
					}, reportInterval, reportInterval);
			}
			
			readPackets();
			socket.close();
		}		
		catch (Exception e) {
			 errorMessage = "Some kinf of error occured!";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
			packetStrucutre.receivedPackets = -1;
			return packetStrucutre;
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
		return packetStrucutre;
	}

	public void stop() {
		logger.addLine(TAG+"Receiving stopped!");
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
		packetStrucutre.receivedPackets = 0;
		packetStrucutre.sentPackets = 0;
		
		//socket.setReceiveBufferSize(1024);//1KB
		//logger.addLine(TAG + "rec buff size: "+ socket.getReceiveBufferSize());
		
		int readedBytes = 0;
		int byteLimit = 0;
		
		try {
			while (reading && inputStream != null/*&& (byteLimit > 0 && totalReadedBytes < byteLimit) */) {
				readedBytes = inputStream.read(byteBuffer);
				totalReadedBytes += readedBytes;
				packetStrucutre.receivedPackets = totalReadedBytes;
				logger.addLine(TAG+"Readed Bytes: "+readedBytes);
				
				/*buffer.append(readedLine+"+");
				if (readedLine.compareTo(TCPSender.END_PACKET) ==  0) {
					if (makePacket(buffer.toString())) {
						logger.addLine(TAG+"Create packet, id: " + packetStrucutre.receivedPackets);					
						try {
							if (parser.getHeadProperty("ID") != null) {
								int id = Integer.parseInt(parser.getHeadProperty("ID"));
								if (ackHandler  != null ) {
									ackHandler.sendAckMessage(socket.getOutputStream(), fileName, id);
									++packetStrucutre.sentPackets;
								}
							}
						}catch (IOException e) {
							logger.addLine(TAG+"Error: ack handler received invalid Id!");
							e.printStackTrace();
						}
						++packetStrucutre.receivedPackets;
					}
					buffer.delete(0, buffer.length());
					readedLine = null;
				}else if (readedLine.compareTo("END") ==  0) {
					reading = false;
					logger.addLine(TAG+"End message received");
				}*/
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
/* it will be useful for another measurement..
	private boolean makePacket(String message) {
		if (parser.parseHttpMessage(message) == false) {
			errorMessage = "Problem occured while parsing message! message: " +message;
			logger.addLine(TAG+errorMessage);
			return false;
		}

		if (fileName== null) {
			fileName = parser.getMethodProperty("URI");
		}else if (!fileName.equals(parser.getMethodProperty("URI"))) {
			errorMessage = "Packet what received is belong to another file! fileName: "+parser.getMethodProperty("URI");
			logger.addLine(TAG+errorMessage);
			return false; 
		}

		String text = parser.getHeadProperty("TEXT");
		String calcedHash = Utility.calcCheckSum(text.getBytes());
		String origHash = null;
		if (parser.getHeadProperty("HASH") != null ) {
			origHash = parser.getHeadProperty("HASH");		
			if (!origHash .equals(calcedHash)) {
				errorMessage = "hash is invalid! calced: "+calcedHash + " original:"+origHash;
				logger.addLine(TAG+errorMessage);
				return false;
			}
		}else{
			errorMessage = "Original hash is invalid!";
			logger.addLine(TAG+errorMessage);			
		}
		
		if (parser.getHeadProperty("ID") != null) {
			packetIds.add(Integer.parseInt(parser.getHeadProperty("ID")));
		}else{
			errorMessage = "Packet's id is invalid!";
			logger.addLine(TAG+errorMessage);
		}
		
		return true;
	}
	*/
}

