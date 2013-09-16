package http.filehandler;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

public class ReportReceiver extends Thread implements ReceiverReportI{
	private final String TAG = "ReportReceiver: ";
	
	private Logger logger = null;	
	private Socket socket = null;
	private boolean isScanStopped = false;	
	private Vector<Integer> reportList = null; 
	
	public ReportReceiver(String id, Logger logger, String serverAddress, int port, Vector<Integer> reports) {
		super();
		this.logger = logger;
		try {
			socket =  new Socket(serverAddress, port);			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reportList = reports;
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
			reportList.add(bytes);
		} else {
			logger.addLine(TAG +"Error ReportReceiver: property is wrong!");				
		}			
	}

	public void run() {
		Scanner scanner = null;
		try {
			if (socket == null) {
				logger.addLine(TAG +"Error ReportReceiver: socket is null!");					
				return;					
			}
			scanner = new Scanner(socket.getInputStream());
			HttpParser parser = new HttpParser(logger);
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
		} catch (SocketException e) {
			logger.addLine(TAG+"Error in ReportHandler: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.addLine(TAG+"Error in ReportHandler: "+e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
		}
	}

	@Override
	public void setReceivedtBytes(int bytes) {
		
	}

	@Override
	public void setSentBytes(int bytes) {
		
	}
	
}	
