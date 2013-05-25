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
	
	public AckHandler(Logger logger){
		this.logger = logger;
	}
	
	public boolean startAckReceiver(String fileName, Socket socket, HashSet<Integer> set) {
		if (receiver == null){
			receiver = new AckReceiver(fileName, logger, socket, set);
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
		logger.addLine("Send message,  sendertId: " + id);
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
		
		public AckReceiver(String fileName, Logger logger, Socket socket, HashSet<Integer> set) {
			super();
			this.fileName = fileName;
			this.logger = logger;
			this.socket =  socket;
			ackList = set;
		}

		public void stopScaning(){
			isScanStopped = true;
		}

		public void run() {
			
			Scanner scanner = null;
			try {
				if (socket == null) {
					logger.addLine("Error AckReceiver: socket is null!!!");
					System.out.println("Error AckReceiver: socket is null!!!");
					return;					
				}
				scanner = new Scanner(socket.getInputStream());
				HttpParser parser = new HttpParser(logger);
				StringBuffer buffer = new StringBuffer();
				System.out.println("ack thread started");
				logger.addLine("Ack thread started,  fileName: "+ fileName);
				while (isScanStopped != true) {
					if (scanner.hasNextLine()){
						String readedLine = scanner.nextLine();
						buffer.append(readedLine+"+");
						if (readedLine.compareTo("END") ==  0) {
							parser.parseHttpMessage(buffer.toString());
							String packetId = parser.getHeadProperty("ACK");
							if (packetId != null &&
									parser.getMethodProperty("URI") != null && parser.getMethodProperty("URI").equals(fileName)){
								int id = Integer.parseInt(packetId); 
								ackList.add(id);
							}
							buffer.delete(0, buffer.length());
						}
					}
				}
			} catch (SocketException e) {
				logger.addLine("Error in AckHandler: "+e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.addLine("Error in AckHandler: "+e.getMessage());
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
