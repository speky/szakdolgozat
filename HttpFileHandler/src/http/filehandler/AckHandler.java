package http.filehandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;

public class AckHandler {

	private AckReceiver receiver = null;
	private Logger logger = null;
	private final String TAG = "AckReceiver: ";
		
	public AckHandler(Logger logger){
		this.logger = logger;		
	}
	
	public boolean startAckReceiver(ICallback callback, String fileName, Socket socket, HashSet<Integer> set) {
		if (receiver == null){
			receiver = new AckReceiver(callback, fileName, logger, socket, set);
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
	
	public void sendAckMessage(OutputStream stream, String fileName, int id){
		PrintWriter printerWriter = new PrintWriter(stream);			
		logger.addLine(TAG +"Send message,  sendertId: " + id);
		StringBuffer buffer = new StringBuffer();
		buffer.append("POST "+ fileName+" HTTP*/1.0\n");
		buffer.append("ACK: "+ id+"\n");
		buffer.append("END\n");
		printerWriter.print(buffer);
		printerWriter.flush();
	}
	
	class AckReceiver extends Thread {
		private Logger logger = null;
		private String fileName = "";
		private Socket socket = null;
		private boolean isScanStopped = false;	
		private HashSet<Integer> ackList = null; 
		private ICallback callback = null;
		
		public AckReceiver(ICallback callback, String fileName, Logger logger, Socket socket, HashSet<Integer> set) {
			super();
			this.fileName = fileName;
			this.logger = logger;
			this.socket =  socket;
			this.callback = callback;
			ackList = set;
		}

		public void stopScaning(){
			isScanStopped = true;
		}

		public void run() {
			
			Scanner scanner = null;
			try {
				if (socket == null) {
					logger.addLine(TAG +"Error AckReceiver: socket is null!!!");					
					return;					
				}
				scanner = new Scanner(socket.getInputStream());
				HttpParser parser = new HttpParser(logger);
				StringBuffer buffer = new StringBuffer();				
				logger.addLine(TAG +"Ack thread started,  fileName: "+ fileName);
				while (isScanStopped != true) {
					if (scanner.hasNextLine()){
						String readedLine = scanner.nextLine();
						buffer.append(readedLine+"+");
						if (readedLine.compareTo("END") ==  0) {
							parser.parseHttpMessage(buffer.toString());
							String packetId = parser.getHeadProperty("ACK");
							if (packetId != null &&
									parser.getMethodProperty("URI") != null && parser.getMethodProperty("URI").equals(fileName)) {
								int id = Integer.parseInt(packetId); 
								ackList.add(id);
								if ( callback != null) {
									callback.receiveAckMessages();
								}
							}
							buffer.delete(0, buffer.length());
						}
					}
				}
			} catch (SocketException e) {
				logger.addLine(TAG+"Error in AckHandler: "+e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.addLine(TAG+"Error in AckHandler: "+e.getMessage());
				e.printStackTrace();
			}
			finally {
				if (scanner != null) {
					scanner.close();
				}
			}
		}
	}
}
