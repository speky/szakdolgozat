package http.filehandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

public class ReportHandler {

	private ReportReceiver receiver = null;
	private Logger logger = null;
	private final String TAG = "ReportReceiver: ";
		
	public ReportHandler(Logger logger){
		this.logger = logger;		
	}
	
	public boolean startReportReceiver(ICallback callback, String id, Socket socket, Vector<Integer> reports) {
		if (receiver == null){
			receiver = new ReportReceiver(callback, id, logger, socket, reports);
			receiver.start();
			return true;
		}
		return false;
	}
	
	public void stopScaning(){
		if (receiver != null) {
			receiver.stopScaning();
		}
	}
	
	public void sendReportMessage(OutputStream stream, String id,  int bytes){
		PrintWriter printerWriter = new PrintWriter(stream);			
		logger.addLine(TAG +"Send report message, id:" + id);
		StringBuffer buffer = new StringBuffer();
		buffer.append("POST "+ id+" HTTP*/1.0\n");
		buffer.append("REPORT: "+ Integer.toString(bytes) +"\n"); 
		buffer.append("END\n");
		printerWriter.print(buffer);
		printerWriter.flush();
	}
	
	class ReportReceiver extends Thread {
		private Logger logger = null;
		private String id = "-";
		private Socket socket = null;
		private boolean isScanStopped = false;	
		private Vector<Integer> reportList = null; 
		private ICallback callback = null;
		
		public ReportReceiver(ICallback callback, String id, Logger logger, Socket socket, Vector<Integer> reports) {
			super();
			this.id = id;
			this.logger = logger;
			this.socket =  socket;
			this.callback = callback;
			reportList = reports;
		}

		public void stopScaning(){
			isScanStopped = true;
		}
		
		private void checkProperty(HttpParser parser) {
			String reportProperty = parser.getHeadProperty("REPORT");
			if (reportProperty != null ) {
				int bytes = Integer.parseInt(reportProperty); 
				reportList.add(bytes);
				if ( callback != null) {
					callback.receiveReportMessages(bytes);
				}
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
				logger.addLine(TAG +"Report thread started, id: "+ id);
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
		
	
	}	
}
