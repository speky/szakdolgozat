package http.testhandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

public class ReportReceiver extends Thread implements ReceiverReportI{
	private final String TAG = "ReportReceiver: ";

	public enum DataType {
		BYTE,
		KB,
		MB
	}

	public enum RateType {
		BITS,
		KBITS,
		MBITS
	}

	private DataType data = DataType.BYTE;
	private RateType rate = RateType.BITS;
	private Logger logger = null;	
	private Socket socket = null;
	private Scanner scanner = null;
	private boolean isScanStopped = false;	
	private ReportI reporter = null;
	private boolean isUpload = false;

	// for testing purposes
	private Vector<TCPReport> tcpReportList = null; 
	private Vector<UDPReport> udpReportList = null;

	public Vector<TCPReport> getTcpReportList() {	
		return tcpReportList;
	}	
	public Vector<UDPReport> getUdpReportList() {
		return udpReportList;
	}

	public ReportReceiver(Logger logger, ReportI reporter, String serverAddress, int port, boolean upload) {
		super();
		this.logger = logger;
		this.reporter = reporter;
		isUpload = upload;
		tcpReportList = new Vector<TCPReport>();
		udpReportList = new Vector<UDPReport>();
		try {
			socket =  new Socket(serverAddress, port);
			scanner = new Scanner(socket.getInputStream());			
		} catch (UnknownHostException e) {
			logger.addLine(TAG +"Error:"+e.getLocalizedMessage());
		} catch (IOException e) {
			logger.addLine(TAG +"Error:"+e.getLocalizedMessage());
		}
	}

	public void setData(DataType type) {
		data = type;
	}

	public void setRate(RateType type) {
		rate = type;
	}
	
	public void sendReportMessage(String id, String type,  String message){					
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(socket.getOutputStream());
		
			StringBuffer buffer = new StringBuffer();
			buffer.append("POST "+ id +" HTTP*/1.0\n");
			buffer.append("REPORT: "+ type +"\n");		
			buffer.append("MESSAGE: "+message+"\n");
			buffer.append("END\n");
			
			printer.print(buffer);
			printer.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}
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
			socket = null;
		} catch (IOException e) {
			logger.addLine(TAG +"Error:"+e.getLocalizedMessage());
		}
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
			report.setData(data);
			report.setRate(rate);
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			calcSpeed(report);
			if (ret) {				
				tcpReportList.add(report);
				reporter.sendMessage("TCP", report.toString());				
			}			
		} else if (reportType.equals("UDP")) {
			UDPReport report = new UDPReport();
			report.setData(data);
			report.setRate(rate);
			ret = report.parseReport(parser.getHeadProperty("MESSAGE"));
			calcSpeed(report);
			if (ret) {
				udpReportList.add(report);
				reporter.sendMessage("UDP", report.toString());
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
						System.out.println(TAG+" receiver report" + buffer.toString());
						parseReport(buffer);
					}
				}				
			}
		} catch (IOException e) {
			logger.addLine(TAG +"Error:"+e.getLocalizedMessage());
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

	// ** receiver is on mobile side => it is download speed regarding to mobile device **
	@Override
	public void setReceivedBytes(final int id, final int interval, final int bytes) {
		TCPReport report = new TCPReport(id, interval, (double)bytes, 0.0, 0.0);
		calcSpeed(report);
		report.setData(data);
		report.setRate(rate);
		tcpReportList.add(report);	
		reporter.sendMessage("TCP", report.toString());
	}

	@Override
	public void setReceivedBytes(final int id, final int interval, final int bytes, final double jitter, final int lost, final int outOfOrdered, final int sum ){		
		UDPReport report = new UDPReport(id, interval, (double)bytes, 0.0, 0.0, jitter, lost, outOfOrdered, sum);
		report.setData(data);
		report.setRate(rate);
		calcSpeed(report);
		udpReportList.add(report);	
		reporter.sendMessage("UDP", report.toString());
	}

	private void calcSpeed(TCPReport report) {

		long time =  report.getInterval();
		double transfered = report.getTransferedData();

		if (isUpload) {
			report.setULSpeed(calculate(time, transfered));
		} else {
			report.setDLSpeed(calculate(time, transfered));
		}

		if (data == DataType.KB) {
			report.setTransferedData(transfered / 1024.0);
		}else if (data == DataType.MB) {
			report.setTransferedData(transfered / (1024.0*1024));
		}
	}

	private double calculate(final long timeInSec, final double bytes) {		
		double transferRate = 0.0;
		if (timeInSec == 0) {
			return transferRate;
		}
		//bits
		transferRate= bytes / (double) timeInSec * 8;

		if (rate == RateType.KBITS) {
			transferRate /= 1000.0; 
		}else if (rate == RateType.MBITS) {
			transferRate /= (1000.0 * 1000.0);
		}		
		return transferRate ;
	}

}	
