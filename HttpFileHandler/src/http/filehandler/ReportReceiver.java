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
	private ReportI reporter = null;
	
	public Vector<TCPReport> getTcpReportList() {
		return tcpReportList;
	}
	
	public Vector<UDPReport> getUdpReportList() {
		return udpReportList;
	}
	
	public ReportReceiver(Logger logger, ReportI reporter, String serverAddress, int port) {
		super();		
		this.logger = logger;
		this.reporter = reporter;
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

	public ReportReceiver(Logger logger, Socket socket, ReportI reporter) {
		super();
		this.logger = logger;
		this.reporter = reporter;
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
		// ** receiver is on server side => it is upload speed regarding to mobile device **
		String reportType = parser.getHeadProperty("REPORT");
		if (reportType.equals("TCP")) {
			TCPReport report = new TCPReport();			
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			if (ret) {
				tcpReportList.add(report);				
				reporter.sendMessage("TCP", report.toString());
				
			}
			
		} else if (reportType.equals("UDP")) {
			UDPReport report = new UDPReport();
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			if (ret) {
				udpReportList.add(report);
				String s = report.toString();
				reporter.sendMessage("UDP", s);
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
	//** receiver is on mobile side => it is download speed regarding to mobile device **
	@Override
	public void setReceivedBytes(final int interval, final int bytes) {
		double dlSpeed = 0.0;
		int id = 0;
		TCPReport report = new TCPReport(id, interval, (double)bytes, dlSpeed, 0.0);	
		tcpReportList.add(report);	
		reporter.sendMessage("TCP", report.toString());
	}

/*	private void calcSpeed() {
		
		long currentTime =  System.currentTimeMillis();
		long ellapsedTime = currentTime - time;
		time = currentTime;

		downloadSpeed = calculate(ellapsedTime, receivedBytes);
		//uploadSpeed = calculate(ellapsedTime, sentBytes);

		previousReceivedBytes = receivedBytes;		
	}

	/ **
	 * 1 byte = 0.0078125 kilobits
	 * 1 kilobits = 0.0009765625 megabit
	 * 
	 * @param time in miliseconds
	 * @param bytes number of bytes downloaded/uploaded
	 * @return SpeedInfo containing current speed
	 * /
	private SpeedInfo calculate(final long time, final long bytes){
		SpeedInfo info = new SpeedInfo();
		if (time == 0) {
			return info;
		}
		info.bps = (bytes / (time / 1000.0) );
		info.kilobits  = info.bps  * BYTE_TO_KILOBIT;
		info.megabits = info.kilobits * KILOBIT_TO_MEGABIT;
		
		return info;
	}
*/
	
}	
