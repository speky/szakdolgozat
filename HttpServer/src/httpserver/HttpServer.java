
package httpserver;

import http.filehandler.ConnectionInstance;
import http.filehandler.HttpParser;
import http.filehandler.Logger;
import http.filehandler.ReportSender;
import http.filehandler.TCPReceiver;
import http.filehandler.TCPSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Specker Zsolt
 */

class ServerThread extends Thread{	
	private static final int MAX_THREAD = 10;
	private static final String TAG = "ServerThread id: ";
	private final int ReportPort = 5000;
	private final int FirstPort = 5555;
	private final int MaxPort = 6000;
	private final int SOCKET_TIMEOUT = 10000; //in milisec	

	private int id = -1;
	private Logger logger = null;
	private HttpParser parser = null;	
	private Properties HeaderProperty = null;
	private int portOffset = 0;
	private ExecutorService pool = null;
	private Set<Future<Integer>> threadSet = new HashSet<Future<Integer>>();
	private int threadCount = 0;
	private ReportSender reporter = null;
	private Vector<ConnectionInstance> connectionInstances = new Vector<ConnectionInstance>();	
	private Socket commandSocket = null;
	private Scanner scanner = null;	
	private Socket testSocket = null;
	private ServerSocket serverSocket = null;
	private PrintWriter printWriter;

	public ServerThread(Logger logger, Socket socket, final int id) {
		super();
		this.logger = logger;
		this.id = id;
		logger.addLine("Start new ServerThread, id: " + id + " IP: " + socket.getInetAddress().getHostAddress());
		parser = new HttpParser(logger);
		HeaderProperty = new Properties();
		pool = Executors.newFixedThreadPool(MAX_THREAD);
		commandSocket = socket;
		try {
			scanner = new Scanner( commandSocket.getInputStream());
			printWriter = new PrintWriter(commandSocket.getOutputStream());
		} catch (IOException e) {
			logger.addLine(TAG + id +"Error at scanner creation: "+e.getMessage());
			e.printStackTrace();
		}
		// the thread start itself 
		start();
	}	

	protected int createSocket() {		
		int port = -1;
		try {
			port = getNextFreePort();
			serverSocket = new ServerSocket(port);			
			// set timer for the accept
			serverSocket.setSoTimeout(SOCKET_TIMEOUT);

		} catch (Exception e) {			
			logger.addLine(TAG+id+" ERROR in run() " + e.getMessage());
			return -1;
		}
		return port;
	}

	public void run() {
		try {   
			while (true) {
				if  (scanner.hasNextLine()){
					logger.addLine(TAG+id+" Get message from client");
					StringBuffer buffer = new StringBuffer();
					String readedLine = scanner.nextLine();
					buffer.append(readedLine);
					while (readedLine.compareTo("END") !=  0) {
						//Read the request line
						readedLine = scanner.nextLine();
						if (readedLine.compareTo("END") !=  0) {
							buffer.append("+"+readedLine);
						}			
					}
					parseClientRequest(buffer.toString());

					if (parser.getMethod().equals("GET")) {		
						int port = 0;
						do {
							port = createSocket();							
							HeaderProperty.put("PORT", Integer.toString(port));
						} while (port == -1);

						makeFileHandlingThread(port);

					} else if (parser.getMethod().equals("STOP")) {
						for (ConnectionInstance instance : connectionInstances ) {
							instance.stop();
						}
					}else{
						sendResponse();
					}	
				}
			}
		} catch (Exception e) {            
			logger.addLine("ERROR in run() " + e.getMessage()+" ( id: " + id+" )");
		} 
	}

	private int getNextFreePort() {
		int nextPort = FirstPort+portOffset;
		++portOffset;
		if (nextPort > MaxPort) {
			nextPort = FirstPort;
			portOffset = 0;
		}
		return nextPort;
	}


	private boolean makeFileHandlingThread(int port) {
		boolean retValue = true;
		if (port <= 0) {
			logger.addLine("Error: Invalid port number!");
			parser.setErrorText("Invalid port number");
			retValue = false;
		}

		if (!parser.getHeadProperty("MODE").equals("DL") && !parser.getHeadProperty("MODE").equals("UL") ||
				!parser.getHeadProperty("CONNECTION").equals("TCP") && !parser.getHeadProperty("CONNECTION").equals("UDP")) {
			logger.addLine("ERROR: wrong connction parameter received!");
			parser.setErrorText("Wrong connction parameter received");
			retValue = false;
		}
		sendResponse();

		if (retValue == false)	{
			return false;
		}

		try {
			testSocket = serverSocket.accept();
			serverSocket.setSoTimeout(0);
		}
		catch (SocketTimeoutException e) {
			logger.addLine(TAG+e.getMessage());
			e.printStackTrace();
		}
		catch (SocketException e) {
			logger.addLine(TAG+e.getMessage());			
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.addLine(TAG+e.getMessage());
			e.printStackTrace();
		}

		reporter = new ReportSender(logger, ReportPort);

		if (parser.getHeadProperty("MODE").equals("DL")){
			if (parser.getHeadProperty("CONNECTION").equals("TCP")){
				int bufferSize = 8000; // 8kb
				String bufferString = parser.getHeadProperty("URI");
				if (bufferString != null) {
					bufferSize = Integer.parseInt(bufferString); 
				}
				TCPSender sender = new TCPSender(logger, ++threadCount, bufferSize);
				if (sender.setSocket(testSocket)) {
					connectionInstances.add(sender);
					Future<Integer> future = pool.submit(sender);
					threadSet.add(future);
				}
			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				//new Thread(new UDPSender(logger, 1 ));
			}
		}else if (parser.getHeadProperty("MODE").equals("UL")){
			if (parser.getHeadProperty("CONNECTION").equals("TCP")){				
				TCPReceiver receiver = new TCPReceiver(logger, ++threadCount, reporter, null);								
				String timer = parser.getHeadProperty("REPORTPERIOD");
				if (timer != null) {
					receiver.setReportInterval(Integer.parseInt(timer)); 
				}				
				if (receiver.setSocket(testSocket)) {
					connectionInstances.add(receiver);
					Future<Integer> future = pool.submit(receiver);
					threadSet.add(future);
				}

			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				//new Thread(new UDPRecever(logger, 1 ));
			}
		}
		reporter.stop();
		reporter = null;
		return true;
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
		printWriter.println(message);
		printWriter.flush();
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

	public static void inreaseConnectionCount() {		
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
				inreaseConnectionCount();
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

