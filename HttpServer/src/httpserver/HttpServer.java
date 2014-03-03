
package httpserver;

import http.testhandler.ConnectionInstance;
import http.testhandler.HttpParser;
import http.testhandler.Logger;
import http.testhandler.ReportSender;
import http.testhandler.TCPReceiver;
import http.testhandler.TCPSender;
import http.testhandler.UDPReceiver;
import http.testhandler.UDPSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
 * @author Specker Zsolt
 */

class ServerThread extends Thread{	
	private static final int MAX_THREAD = 5;
	private static final String TAG = "ServerThread id: ";
	private final int ReportPort = 5000;
	private final int FirstPort = 5500;
	private final int MaxPort = 5600;
	private final int SOCKET_TIMEOUT = 5000; //in milisec	

	private int id = 1;
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

	public ServerThread(Logger logger, Socket socket) {
		super();
		this.logger = logger;		
		logger.addLineAndPrint("Start new ServerThread, id: " + id + " IP: " + socket.getInetAddress().getHostAddress());
		parser = new HttpParser(logger);
		HeaderProperty = new Properties();
		pool = Executors.newFixedThreadPool(MAX_THREAD);
		commandSocket = socket;
		try {
			scanner = new Scanner( commandSocket.getInputStream());
			printWriter = new PrintWriter(commandSocket.getOutputStream());
		} catch (IOException e) {
			logger.addLineAndPrint(TAG + id +"Error at scanner creation: "+e.getMessage());
			e.printStackTrace();
		}
		// the thread start itself 
		start();
	}	

	private int getNextFreePort() {
		int nextPort = FirstPort + portOffset;
		++portOffset;
		if (nextPort > MaxPort) {
			nextPort = FirstPort;
			portOffset = 1;
		}
		return nextPort;
	}
	
	protected int createSocket() {		
		int port = -1;
		try {
			port = getNextFreePort();
			// re-use the port thus  test instances could attach to this port many times
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
			
			// set timer for the accept
			serverSocket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (Exception e) {
			logger.addLineAndPrint(TAG + id + " ERROR in createsocket() port: "+ port + " " + e.getMessage());
			return -1;
		}
		return port;
	}
		
	public void run() {
		try {   
			while (true) {
				if  (scanner.hasNextLine()){
					logger.addLineAndPrint(TAG+id+" Get message from client");
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
						
						makeTestHandlingThread(port);
						
					} else if (parser.getMethod().equals("STOP")) {
						for (ConnectionInstance instance : connectionInstances ) {
							if (instance != null) {
								instance.stop();		
							}
						}
						connectionInstances.clear();
						reporter.stop();
						reporter = null;						
					}else{
						sendResponse();
					}	
				}
			}
		} catch (Exception e) {            
			logger.addLineAndPrint(TAG + id +"ERROR in run() " + e.getMessage()+ " ");
		} 
	}
	
	private void addConnectionInstance(ConnectionInstance instance) {
		connectionInstances.add(instance);
		Future<Integer> future = pool.submit(instance);
		threadSet.add(future);
	}

	private boolean checkAndSendResponse(final int port) {
		if (port <= 0) {
			logger.addLineAndPrint("Error: Invalid port number!");
			parser.setErrorText("Invalid port number");
			return false;
		}

		if (!parser.getHeadProperty("MODE").equals("DL") && !parser.getHeadProperty("MODE").equals("UL") ||
				!parser.getHeadProperty("CONNECTION").equals("TCP") && !parser.getHeadProperty("CONNECTION").equals("UDP")) {
			logger.addLineAndPrint("ERROR: wrong connction parameter received!");
			parser.setErrorText("Wrong connction parameter received");
			
		}
		sendResponse();
		return true;
	}
	
	private void makeTestHandlingThread(final int port) {

		if (checkAndSendResponse(port) == false)	{
			return ;
		}

		try {
			testSocket = serverSocket.accept();
			serverSocket.setSoTimeout(0);
		}
		catch (SocketTimeoutException e) {
			logger.addLineAndPrint(TAG+e.getMessage());
			e.printStackTrace();
		}
		catch (SocketException e) {
			logger.addLineAndPrint(TAG+e.getMessage());			
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.addLineAndPrint(TAG+e.getMessage());
			e.printStackTrace();
		}
		
		reporter = new ReportSender(logger, ReportPort);
		
		if (reporter.isSocketConnected() == false) {
			reporter = null;
			return;
		}
		
		int bufferSize = 1000; // 8kb
		String bufferString = parser.getHeadProperty("BUFFERSIZE");
		if (bufferString != null) {
			bufferSize = Integer.parseInt(bufferString); 
		}
		
		int time = 1000; 
		if (parser.getHeadProperty("REPORTPERIOD") != null) {
			time = Integer.parseInt(parser.getHeadProperty("REPORTPERIOD"));
		}
		
		if (parser.getHeadProperty("MODE").equals("DL")){
			if (parser.getHeadProperty("CONNECTION").equals("TCP")){				
				TCPSender sender = new TCPSender(logger, ++threadCount, bufferSize);
				if (sender.setSocket(testSocket)) {					
					addConnectionInstance(sender);
				}
			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				UDPSender sender  = new UDPSender(++threadCount, logger, bufferSize);				
				if (sender.setReceiverParameter(port)) {
					addConnectionInstance(sender);
				}
			}
		}else if (parser.getHeadProperty("MODE").equals("UL")){
			if (parser.getHeadProperty("CONNECTION").equals("TCP")){
				TCPReceiver receiver = new TCPReceiver(logger, ++threadCount, reporter, null);				
				receiver.setReportInterval(time);				
				if (receiver.setSocket(testSocket)) {
					addConnectionInstance(receiver);
				}

			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				UDPReceiver receiver  = new UDPReceiver(++threadCount, logger, reporter, null, time, bufferSize);
				if (receiver.setTestPort(port)) {
					addConnectionInstance(receiver);
				}
			}
		}		
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

			if (!(parser.getMethod().equals("GET"))) {
				String responseText = response.setResponseText(HttpParser.HTTP_BADREQUEST, HttpResponse.MIME_PLAINTEXT, null);
				sendMessageToClient(responseText);
				return false;
			}

			String responseText = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, HeaderProperty);
			sendMessageToClient(responseText);
			return true;
		}catch (Exception e){
			logger.addLineAndPrint("response error: " + e.getMessage());
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

	private static final int SERVER_PORT = 4500;
	private static final String TAG = "HTTP_Server: ";

	private static ServerSocket serverSocket = null;
	private static Logger logger = null;

	public static void main(String[] args) {		
		logger = new Logger("server.log");		
		try	{
			// re-use the port thus  HttpService instances could attach to this port many times
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(SERVER_PORT));
			
			while (true) {
				// wait for client connection
				logger.addLineAndPrint(TAG+"Waiting for connection on port:"+ SERVER_PORT +"\n");
				Socket socket = serverSocket.accept();	
				//figure out what is the ip-address of the client
				InetAddress client = socket.getInetAddress();
				//and print it to log
				logger.addLineAndPrint(TAG+client + " connected to server.\n");
				// start thread for handling a client
				new Thread(new ServerThread(logger, socket));				
			}
		} catch (Exception e) {
			System.out.println("Thread hiba: " + e.getMessage());
		} finally {
			try {
				serverSocket.close();
				logger.addLineAndPrint("Server socket closed");
				logger.closeFile();
				System.out.println("Connection closed");
			} catch (Exception e) {
				System.err.println("Hiba a kapcsolat lezarasa kozben.");
			}
		}
	}

}

