package http.filehandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;


public class TCPSender  extends ConnectionInstance implements ICallback{	
		
	private byte[] byteBuffer;	
	private Socket socket = null;
	private PrintWriter printerWriter = null;
	private ReportHandler reportHandler = null;
	private Vector<Integer> reportList = new Vector<Integer>();
	private boolean running = true;	
	private final String TAG = "TCPSender: ";	
	private final int ACK_WAITING = 5000; //in milisec
	public static final String END_PACKET = "END_PACKET";	
	
	public TCPSender(Logger logger, final int id, final int bufferSize) {
		super(ConnectionInstance.TCP_SENDER, id, logger);				
		logger.addLine(TAG+ "Created, id: " + id);
		
		reportHandler = new ReportHandler(logger);
		packetStructure = new PacketStructure();
		packetStructure.id = id;
		byteBuffer = new byte[bufferSize];
		Utility.fillStringBuffer(byteBuffer, bufferSize);		
	}

	@Override
	public void receiveReportMessages(int receivedBytes) {
		if (packetStructure != null ) {
			packetStructure.receivedPackets = receivedBytes;
		}
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
		reportHandler.stopScaning();
		reportHandler = null;
		packetStructure = null;
		running = false;
		try {
			socket.close();			
		} catch (IOException e) {
			errorMessage = "Socket cannot stopped!";
			packetStructure.receivedPackets = -1;
			
			logger.addLine(TAG+errorMessage);
			e.printStackTrace();
		}
	}
	
	public PacketStructure call() {		
		try {			
			if (checkPrerequisite() == false) {
				packetStructure.receivedPackets = -1;
				return packetStructure;
			}
			
			reportHandler.startReportReceiver(this, Integer.toString(id), socket, reportList);
			OutputStream outputStream = socket.getOutputStream();			
			logger.addLine(TAG+"Send message,  sendertId: " + id);			

			packetStructure.receivedPackets = 0;
			
			
			while (running) {
				 outputStream.write(byteBuffer);
				 outputStream.flush();				 
			}
			
			logger.addLine(TAG+" Sending ended!");
						
			reportHandler.stopScaning();
			logger.addLine(TAG+"Received report message: "+packetStructure.receivedPackets);
		} catch (Exception e) {
			errorMessage = "Error occured in sending packets";
			packetStructure.receivedPackets = -1;
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
		}
		finally{
			try {
				reportHandler.stopScaning();
				reportHandler = null;				
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				errorMessage = "Socket cannot stopped!";
				packetStructure.receivedPackets = -1;
				logger.addLine(TAG+errorMessage);
				e.printStackTrace();
			}
		}
		return packetStructure;
	}

	private boolean checkPrerequisite() {
		if (socket == null) {
			logger.addLine(TAG+"Id: " + id+ " Connection problem!");			
			return false;
		}
		
		return true;
	}

	
}
