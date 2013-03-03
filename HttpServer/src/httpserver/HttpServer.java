
package httpserver;

import http.filehandler.HttpFileHandler;
import http.filehandler.HttpParser;
import http.filehandler.Logger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

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
	private HttpFileHandler fileHandler = null;
	private Properties HeaderProperty = null;
	private final int FirstPort = 5000;
	private final int MaxPort = 5050;
	private int portOffset = 0;
	
	public ServerThread(Logger logger, Socket socket, final int id) {
		super();
		// register a new peer
		client = new Client(socket, id, 0);
		this.logger = logger;
		fileHandler = new HttpFileHandler(logger);
		fileHandler.addFile("test.txt");
		logger.addLine("Add a client, id: " + client.id + " IP: " + socket.getInetAddress().getHostAddress());
		parser = new HttpParser(logger);
		HeaderProperty = new Properties();
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
					int nextPort = getNextPort();
					HeaderProperty.put("PORT", nextPort);
					sendResponse();
					if (parser.getMethod().equals("GET")){						
						makeFileHandlingThread(nextPort);
						
					}
						
				}
			}
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage()+" (client id " + client.id+" )");
		} 
	}
	
	private int getNextPort() {
		int nextPort = FirstPort+portOffset;
		portOffset++;
		if (nextPort > MaxPort) {
			nextPort = FirstPort;
			portOffset = 0;
		}
		return nextPort;
	}
	
	private void  makeFileHandlingThread(int port) {
		
	}
	
	private boolean sendResponse() {
		try {
			HttpResponse response = new HttpResponse(logger);
			if (parser == null || parser.getMethod() == null) {
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
				String responseText = response.setResponseText(HttpParser.HTTP_BADREQUEST, HttpResponse.MIME_PLAINTEXT, null);
				sendMessageToClient(responseText);
				return false;
			}

			if (parser.getMethod().equals("PING")) {
				String responseText = response.setResponseText("200 PONG", null, null);
				System.out.println("uzenet a kliensnek");
				sendMessageToClient(responseText);
				return true;
			}

			String responseText = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, HeaderProperty);
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
		parser.parseHttpMessage(readedLine);
	}
}

public class HttpServer {

	private static final int SERVER_PORT = 4444;
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

