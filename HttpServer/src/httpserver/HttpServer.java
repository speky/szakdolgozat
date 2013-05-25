
package httpserver;

import http.filehandler.FileInstance;
import http.filehandler.HttpFileHandler;
import http.filehandler.HttpParser;
import http.filehandler.Logger;
import http.filehandler.TCPReceiver;
import http.filehandler.TCPSender;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	private final int FirstPort = 5555;
	private final int MaxPort = 6000;
	private int portOffset = 0;
	private ExecutorService pool = null;
	private Set<Future<Integer>> threadSet = new HashSet<Future<Integer>>();
	private int threadCount = 0;
	static final int MAX_THREAD = 10;


	public ServerThread(Logger logger, Socket socket, final int id) {
		super();

		this.logger = logger;
		client = new Client(socket, id, 0);
		fileHandler = new HttpFileHandler(logger);
		fileHandler.addFile("test.txt");
		logger.addLine("Add a client, id: " + client.id + " IP: " + socket.getInetAddress().getHostAddress());
		parser = new HttpParser(logger);
		HeaderProperty = new Properties();
		pool = Executors.newFixedThreadPool(MAX_THREAD);

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

					int nextPort = 0;
					if (parser.getMethod().equals("GET")){						
						if (parser.getHeadProperty("MODE").equals("DL")){
							nextPort = getNextPort();
							HeaderProperty.put("PORT", Integer.toString(nextPort));
						}
						sendResponse();
						makeFileHandlingThread(nextPort);						
					}else{
						sendResponse();
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

	private FileInstance checkPacketSize(String uri, String packet) {		 
		FileInstance file = HttpServer.fileList.get(uri);
		if (file == null) {
			logger.addLine("Error: File does not exist!, name: "+uri);
			return null;
		}
		int packetSize = FileInstance.DEFAULT_SIZE;
		if (packet != null ){
			packetSize = Integer.parseInt(packet);
		}		
		if (FileInstance.DEFAULT_SIZE != packetSize){
			file.splitFileToPockets(packetSize);
		}		
		return file;
	}

	private void  makeFileHandlingThread(int port) {
		if (port == 0) {
			logger.addLine("Error: Invalid port number!");
			return;
		}

		FileInstance file = checkPacketSize(parser.getMethodProperty("URI"), parser.getHeadProperty("PACKET_SIZE"));
		if (file == null) {
			return;
		}

		if (parser.getHeadProperty("MODE").equals("DL")){
			if (parser.getHeadProperty("CONNECTION").equals("TCP")){
				TCPSender sender = new TCPSender(logger, threadCount++);				
				sender.setFile(file);
				sender.setReceiverParameters(port, "192.168.0.101");//"10.158.243.47");//10.0.2.15");//client.socket.getInetAddress().getHostAddress());
				Future<Integer> future = pool.submit(sender);
				threadSet.add(future);
			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				//new Thread(new UDPSender(logger, 1 ));
			}else {
				logger.addLine("ERROR: wrong connction parameter received!");
				return;
			}
		}else if (parser.getHeadProperty("MODE").equals("UL")){
			if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				Callable<Integer> callable = new TCPReceiver(logger, threadCount++);
				Future<Integer> future = pool.submit(callable);
				threadSet.add(future);
			} else if (parser.getHeadProperty("CONNECTION").equals("UDP")){
				//new Thread(new UDPRecever(logger, 1 ));
			}else {
				logger.addLine("ERROR: wrong mode parameter received!");		
				return;
			}
		}

		for (Future<Integer> future : threadSet) {
			try {
				logger.addLine("A thread ended, value: " + future.get());
				System.out.println("value: "+ future.get());			
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public static HashMap<String, FileInstance> fileList = new HashMap<String, FileInstance> ();

	private static void makeFileList() {
		File filePath = new File(System.getProperty("user.dir") + "\\asset");		
		File[] listOfFiles = filePath.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fileName = file.getName();
				int i = fileName.lastIndexOf('.');
				String extension = null; 
				if (i > 0) {
					extension = fileName.substring(i+1);
				}
				if (extension != null && extension.equals("bin")){
					System.out.println("File readed, " + fileName);
					FileInstance fileInst = new FileInstance(logger, filePath+"\\"+fileName);		
					fileInst.splitFileToPockets(FileInstance.DEFAULT_SIZE);
					fileList.put(fileName, fileInst);
				}
			}
		}
	}

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
			makeFileList();
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

