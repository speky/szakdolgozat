
package httpserver;

import http.filehandler.FileInstance;
import http.filehandler.Logger;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;

/**
 *
 * @author Specker Zsolt
 */

class Client {
	public Socket socket;
	public PrintWriter printWriter;
	public Scanner scanner;
	public int id;
	public int port;
	public HashSet<String> files;

	Client(Socket socket, final int id, final int port) {
		try{
			this.socket = socket;
			this.id = id;
			this.port = port;
			printWriter = new PrintWriter(socket.getOutputStream());
			scanner = new Scanner(socket.getInputStream());			
		}
		catch(Exception e){
			System.out.println("peer hiba: " + e.getMessage() + " Peer id " + id);
		}
	}
}

class ServerThread extends Thread {	
	private Client client = null;
	private Logger logger = null;
	private HttpParser parser = null;
	private HashSet<FileInstance> files;

	public ServerThread(Logger logger, Socket socket, final int id) {
		super();
		// register a new peer
		client = new Client(socket, id, 0);
		this.logger = logger;
		logger.addLine("Add a client, id: " + client.id + " IP: " + socket.getInetAddress().getHostAddress());
		parser = new HttpParser(logger);
		files = new  HashSet<FileInstance>();
		start();
	}

	public void run() {
		try {   
			while (true) {
				if  (client.scanner.hasNextLine()){
					logger.addLine("Get message from client,  clientId: " + client.id+"\n");
					StringBuffer buffer = new StringBuffer();
					String readedLine = client.scanner.nextLine();
					buffer.append(readedLine);
					while (readedLine.compareTo("END") !=  0) {
						//Read the request line
						readedLine = client.scanner.nextLine();
						if (readedLine.compareTo("END") !=  0) {
							buffer.append("+"+readedLine);
						}			
					}
					parseClientRequest(buffer.toString());
					sendResponse(parser);
					if (parser.getMethod().equals("GET")){						
						// .. fogado szál inditása
					}else if (parser.getMethod().equals("SEND")){
						if (isFileInSet(parser.getMethodProperty("URI"))){
							// küldö szál inditása							
						}
					}
				}
			}
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage()+" (client id " + client.id+" )");
		} 
	}

	private boolean isFileInSet(final String name) {
		for (FileInstance fi : files){
			if (fi.name.equals(name))                
				return true;
		}
		return false;
	}
	private boolean sendResponse(HttpParser parser) {
		try {
			HttpResponse response = new HttpResponse(logger);
			if (parser.getMethod() == null) {
				return false;
			}
			response.PrintProperties(parser.getMethodProperty("uri"), parser.getMethod(), parser.getHeadProperty(), null);

			String errorMessage = parser.getErrorText();
			if (errorMessage != null) {
				String responseText = response.setResponseText(errorMessage, HttpResponse.MIME_PLAINTEXT, null);
				sendMessageToClient(responseText);
				return false;
			}

			if (!(parser.getMethod().equals("PING") || parser.getMethod().equals("GET"))) {
				String responseText = response.setResponseText(HttpResponse.HTTP_BADREQUEST, HttpResponse.MIME_PLAINTEXT, null);
				sendMessageToClient(responseText);
				return false;
			}

			if (parser.getMethod().equals("PING")) {
				String responseText = response.setResponseText("200 PONG", null, null);
				System.out.println("uzenet a kliensnek");
				sendMessageToClient(responseText);
				return true;
			}

			String responseText = response.setResponseText(HttpResponse.HTTP_OK, HttpResponse.MIME_PLAINTEXT, null);
			sendMessageToClient(responseText);
			return true;
		}catch (Exception e){
			logger.addLine("response error: " + e.getMessage());
		}
		return false;
	}

	private boolean sendMessageToClient(final String message) {
		client.printWriter.println(message);
		client.printWriter.flush();
		return true;
	}

	private void parseClientRequest(String readedLine) {		
		//Read the http request from the client from the socket interface into a buffer.
		parser.parseHttpHead(readedLine);
	}

	private FileInstance AddFile(final String fileName) {
		if (fileName == null){
			logger.addLine("fileName is empty!");
			return null;
		}
		for (FileInstance fi : files){
			if (fi.name.equals(fileName))                
				logger.addLine("File has already added: " + fileName);
			return fi;
		}
		logger.addLine("reg new file: " + fileName);
		FileInstance file = new FileInstance(logger, fileName);
		files.add(file);
		return file;
	}
}

public class HttpServer {

	private static final int SERVER_PORT = 13000;
	private static final String TAG = "HTTP_Server: ";

	private static ServerSocket serverSocket = null;
	private static Logger logger = null;
	private static int activeConnections;

	public static  void decreaseConnectounCount() {
		if  (activeConnections > 0) {
			--activeConnections;
			logger.addLine(TAG+" decrease connections: "+ activeConnections);
		}
	}

	public static void inreaseConnectounCount() {		
		++activeConnections;
		logger.addLine(TAG+" increase connections: "+ activeConnections);
	}

	public static void main(String[] args) {
		activeConnections = 0;
		logger = new Logger("");		
		try	{
			serverSocket = new ServerSocket(SERVER_PORT);

			while (true) {
				// wait for client connection
				Socket socket = serverSocket.accept();				
				//figure out what is the ip-address of the client
				InetAddress client = socket.getInetAddress();
				//and print it to log
				logger.addLine(TAG+client + " connected to server.\n");
				// start thread for handling a client
				new Thread(new ServerThread(logger, socket, activeConnections));
				inreaseConnectounCount();
			}
		} catch (Exception e) {
			System.out.println("Thread hiba: " + e.getMessage());
		} finally {
			try {
				serverSocket.close();
				logger.addLine("Server socket closed");
				logger.closeFile();
				System.out.println("Connection closed");
			} catch (Exception e) {
				System.err.println("Hiba a kapcsolat lezarasa kozben.");
			}
		}
	}

}
