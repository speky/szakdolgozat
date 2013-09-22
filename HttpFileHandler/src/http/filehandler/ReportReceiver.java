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
	private Scanner scanner = null;
	private boolean isScanStopped = false;	
	private Vector<TCPReport> tcpReportList = null; 
	private Vector<UDPReport> udpReportList = null;
	
	public Vector<TCPReport> getTcpReportList() {
		return tcpReportList;
	}
	
	public Vector<UDPReport> getUdpReportList() {
		return udpReportList;
	}
	
	public ReportReceiver(Logger logger, String serverAddress, int port) {
		super();
		this.logger = logger;
		try {
			socket =  new Socket(serverAddress, port);
			scanner = new Scanner(socket.getInputStream());			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tcpReportList = new Vector<TCPReport>();
		udpReportList = new Vector<UDPReport>();
	}

	public ReportReceiver(Logger logger, Socket socket) {
		super();
		this.logger = logger;
		this.socket =  socket;
		tcpReportList = new Vector<TCPReport>();
		udpReportList = new Vector<UDPReport>();
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
	
	private boolean checkProperty(HttpParser parser) {
		if (parser == null) {
			logger.addLine(TAG +"Error:parser is null!");
			return false;
		}
		if (null == parser.getHeadProperty("REPORT") ) {
			logger.addLine(TAG +"Error:report type is null!");
			return false;
		}
		boolean ret = true;
		// parse report message and add them to report list
		String reportType = parser.getHeadProperty("REPORT");
		if (reportType.equals("TCP")) {
			TCPReport report = new TCPReport();
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			if (ret) {
				tcpReportList.add(report);
			}
			
		} else if (reportType.equals("UDP")) {
			UDPReport report = new UDPReport();
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			if (ret) {
				udpReportList.add(report);
			}
			
		} else {
			logger.addLine(TAG +"Error:type is invalid: " + reportType);
			return false;
		}
		return ret;		
	}

	public boolean parseReport (StringBuffer buffer) {		
		HttpParser parser = new HttpParser(logger);
		boolean ret = true;
		ret = parser.parseHttpMessage(buffer.toString());
		ret = checkProperty(parser);
		buffer.delete(0, buffer.length());		
		parser = null;
		buffer = null;
		return  ret;
	}
	
	public void receiveReport() {
		try {
			if (scanner == null) {
				scanner = new Scanner(socket.getInputStream());
			}
			StringBuffer buffer = new StringBuffer();
			logger.addLine(TAG +"Report thread started");
			while (isScanStopped != true) {
				if (scanner.hasNextLine()){
					String readedLine = scanner.nextLine();			
					buffer.append(readedLine+"+");
					if (readedLine.compareTo("END") ==  0) {
						parseReport(buffer);
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
