package http.filehandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

public class ReportReceiver extends Thread implements ReceiverReportI{
	private final String TAG = "ReportReceiver: ";
	
	private Logger logger = null;	
	private Socket socket = null;
	private boolean isScanStopped = false;	
	private Vector<TCPReport> tcpReportList = null; 
	private Vector<UDPReportTest> udpReportList = null;
	
	public ReportReceiver(Logger logger, String serverAddress, int port) {
		super();
		this.logger = logger;
		try {
			socket =  new Socket(serverAddress, port);			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tcpReportList = new Vector<TCPReport>();
		udpReportList = new Vector<UDPReportTest>();
	}

	public ReportReceiver(Logger logger, Socket socket) {
		super();
		this.logger = logger;
		this.socket =  socket;
		tcpReportList = new Vector<TCPReport>();
		udpReportList = new Vector<UDPReportTest>();
	}
	
	public void stopScaning(){
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket = null;
		isScanStopped = true;
	}
	
	private void checkProperty(HttpParser parser) {
		String reportProperty = parser.getHeadProperty("REPORT");
		if (reportProperty != null ) {
			int bytes = Integer.parseInt(reportProperty); 
			//reportList.add(bytes);
		} else {
			logger.addLine(TAG +"Error ReportReceiver: property is wrong!");				
		}			
	}

	public void receiveReport() {
		HttpParser parser = new HttpParser(logger);
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream());		
			StringBuffer buffer = new StringBuffer();
			logger.addLine(TAG +"Report thread started");
			while (isScanStopped != true) {
				if (scanner.hasNextLine()){
					String readedLine = scanner.nextLine();
					buffer.append(readedLine+"+");
					if (readedLine.compareTo("END") ==  0) {
						parser.parseHttpMessage(buffer.toString());
						checkProperty(parser);
						buffer.delete(0, buffer.length());
					}
				}
			}
		} catch (IOException e) {
			logger.addLine(TAG+ e.getMessage());
		}
		finally {
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
		}

	}
	
	public void run() {
		if (socket == null) {
			logger.addLine(TAG +"Error ReportReceiver: socket is null!");					
			return;					
		}
		receiveReport();
	
	}

	@Override
	public void setReceivedBytes(int bytes) {
		
	}

	@Override
	public void setSentBytes(int bytes) {
		
	}
	
}	
