package com.drivetesting.services;

import http.testhandler.ConnectionInstance;
import http.testhandler.Logger;
import http.testhandler.ReportI;
import http.testhandler.ReportReceiver;
import http.testhandler.ReportReceiver.DataType;
import http.testhandler.ReportReceiver.RateType;
import http.testhandler.TCPReceiver;
import http.testhandler.TCPSender;
import http.testhandler.UDPReceiver;
import http.testhandler.UDPSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.drivetesting.DriveTestApp;

public class HttpService extends IntentService implements ReportI {
	public static final int MAX_THREAD = 10;
	private static final int SOCKET_TIMEOUT = 1000;

	private final String TAG = "HttpClient: ";
	private final int ServerPort = 4500;
	private final int ReportPort = 5000;

	private  String serverAddress = null;
	private Logger logger;
	private ExecutorService pool = null;
	private Set<Future<Integer>> threadSet = new HashSet<Future<Integer>>();
	private int threadCount = 0;
	private static Socket socket = null;
	private Scanner scanner;
	private PrintWriter printWriter = null;
	private Properties answerProperty = new Properties();
	private Properties headerProperty = new Properties();
	private ArrayList<ConnectionInstance> connectionInstances = new ArrayList<ConnectionInstance>();
	private ReportReceiver reportReceiver = null;
	private String errorMessage = null;
	private int type = 0;
	private int direction = 0;
	private int bufferSize = 8000;
	private int reportPeriod = 1000;
	private int udpRate = 1024;
	private int rateType = 1;

	private Messenger messenger = null;

	public void sendMessage(final String key, final String value) {
		if (messenger != null) {
			Message m = Message.obtain();			
			Bundle b = new Bundle();
			b.putString(key, value);
			m.setData(b);
			try {
				Log.d(TAG, key + " "+ value);
				messenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public  HttpService() {
		super("HttpClientIntentService");				
		logger = new Logger("");		
		pool = Executors.newFixedThreadPool(MAX_THREAD);
	}

	private Socket createSocket(int port) {
		try {			
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(serverAddress, port), SOCKET_TIMEOUT);
			logger.addLine(TAG + " Create new socket");
			return socket;
		} catch (UnknownHostException e) {
			errorMessage  = "Test socket creating problem";
			logger.addLine(TAG + "ERROR in run() " + e.getMessage());
			//sendMessage("error", errorMessage);			
		} catch (IOException e) {
			errorMessage = "Test socket creating problem (I/O)";
			logger.addLine(TAG + "ERROR in run() " + e.getMessage());
			//sendMessage("error", errorMessage);			
		}
		return null;
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		messenger = (Messenger) intent.getExtras().get("handler"); 		
	
		serverAddress = (String)intent.getExtras().get("serverIp");
		if (serverAddress == null || serverAddress.equals("0.0.0.0")) {
			sendMessage("error", "Error: Invalid server address!");
			System.out.println("Invalid server address!");
		}

		type = (Integer)intent.getExtras().get("type");

		if (type != DriveTestApp.UDP && type != DriveTestApp.TCP) {
			sendMessage("error", "Error: Invalid protocol type!");
			System.out.println("Invalid protocol type!");
		}

		direction = (Integer)intent.getExtras().get("direction");
		if (direction != DriveTestApp.DOWNLOAD && direction != DriveTestApp.UPLOAD) {
			sendMessage("error", "Error: Invalid direction!");
			System.out.println("Invalid direction!");
		}

		bufferSize = Integer.parseInt((String)intent.getExtras().get("bufferSize"));
		reportPeriod = Integer.parseInt((String)intent.getExtras().get("reportPeriod"));
		udpRate = Integer.parseInt((String)intent.getExtras().get("udpRate"));
		rateType = Integer.parseInt((String)intent.getExtras().get("rateType"));

		try {
			if (null == socket ) {
				socket = createSocket(ServerPort);
			}
			scanner = new Scanner(socket.getInputStream());
			printWriter = new PrintWriter(socket.getOutputStream());

			makeNewThread();
		}catch (Exception e) {
			e.printStackTrace();
			sendMessage("error", "Error: Cannot connect to server! IP: "+  serverAddress +" port: "+ ServerPort );
			pool.shutdownNow();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stop();
	}

	public void stop() {
		logger.addLine(TAG+ "send stop to server");
		try {
			sendMessageToServer("STOP / HTTP*/1.0\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addLine(TAG+ "stop threads");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (ConnectionInstance instance : connectionInstances) {
			instance.stop();
		}		
		pool.shutdownNow();
	}

	private void addConnectionInstance(ConnectionInstance instance) {
		connectionInstances.add(instance);		
		Future<Integer> future = pool.submit(instance);
		threadSet.add(future);
	}

	public void makeNewThread() {
		try {
			
			logger.addLine(TAG+"makeNewThread" );
			
			boolean isUpload = false;
			if (type == DriveTestApp.TCP) {
				if (direction == DriveTestApp.DOWNLOAD) {
					isUpload = true;
					sendMessageToServer("GET / HTTP*/1.0\nREPORTPERIOD: "+reportPeriod+"\nMODE: DL\n CONNECTION: TCP\nUnit: "+rateType+"\nBUFFERSIZE: " + bufferSize +"\n");
				} else {
					sendMessageToServer("GET / HTTP*/1.0\nREPORTPERIOD: "+reportPeriod+"\nMODE: UL\n CONNECTION: TCP\nBUFFERSIZE: " + bufferSize +"\n");
				}
			}else {
				if (direction == DriveTestApp.DOWNLOAD) {
					isUpload = true;
					sendMessageToServer("GET / HTTP*/1.0\nREPORTPERIOD: "+reportPeriod+"\nMODE: DL\n CONNECTION: UDP\nBUFFERSIZE: " + bufferSize + "\nUDPRATE:"+ udpRate +"\n");
				} else {					
					sendMessageToServer("GET / HTTP*/1.0\nREPORTPERIOD: "+reportPeriod+"\nMODE: UL\n CONNECTION: UDP\nBUFFERSIZE: " + bufferSize + "\nUDPRATE:"+ udpRate +"\n");
				}
			}					

			receiveMessageFromServer();
			int testPort = Integer.parseInt(headerProperty.getProperty("PORT")); 
			if (!answerProperty.getProperty("CODE").equals("200") && answerProperty.getProperty("TEXT").equals("OK")) {
				logger.addLine(TAG+ "Bad answer from server, text:"+answerProperty.getProperty("TEXT"));
				sendMessage("error", "Server reject the test: "+ answerProperty.getProperty("TEXT"));				
			}

			Socket socket = createSocket(testPort);
			if (socket == null) {
				sendMessage("error", "Could not connect to server! test port: "+ testPort);
				return;
			}

			reportReceiver = new ReportReceiver(logger, this, serverAddress, ReportPort, isUpload);
			switch (rateType) {
			case 1:
				reportReceiver.setData(DataType.BYTE);
				reportReceiver.setRate(RateType.BITS);						
				break;
			case 2:
				reportReceiver.setData(DataType.KB);
				reportReceiver.setRate(RateType.KBITS);				
				break;
			case 3:
				reportReceiver.setData(DataType.MB);
				reportReceiver.setRate(RateType.MBITS);				
				break;
			}			
			reportReceiver.start();

			if (type == DriveTestApp.TCP) {
				if (direction == DriveTestApp.DOWNLOAD) {
					TCPReceiver receiver = new TCPReceiver(logger, ++threadCount, null, reportReceiver);
					receiver.setReportInterval(reportPeriod);
					if (receiver.setSocket(socket)) {					
						addConnectionInstance(receiver);
					}
				} else {
					TCPSender sender = new TCPSender(logger, ++threadCount, bufferSize);
					if (sender.setSocket(socket)){
						addConnectionInstance(sender);
					}
				}
			} else {
				if (direction == DriveTestApp.DOWNLOAD) {
					UDPReceiver receiver = new UDPReceiver(++threadCount, logger, null,  reportReceiver, reportPeriod,  bufferSize);
					if (receiver.setSenderParameters(testPort, serverAddress)) {
						addConnectionInstance(receiver);
					}

				} else {
					UDPSender sender = new UDPSender(++threadCount, logger, bufferSize);					
					if (sender.setReceiverParameters(testPort, serverAddress)) {
						// udprate in kbits
						sender.setRateInBitsPerSec(udpRate * 1024);
						addConnectionInstance(sender);
					}
				}
			}						

			for (Future<Integer> futureInst : threadSet) {
				try {					
					Integer value = futureInst.get();
					logger.addLine(TAG+"A thread ended, value: " + value);										
					ConnectionInstance instance = getConnectionInstances(value);
					if (instance == null) {
						sendMessage("error", "Error: Invalid id!");
						return;
					}						
					sendMessage("end", "Test end");					
					deleteInstance(instance);
				} catch (ExecutionException e) {
					e.printStackTrace();
					sendMessage("error", "Error: " + e.getMessage());					
					pool.shutdownNow();
				} catch (InterruptedException e) {					
					e.printStackTrace();
					sendMessage("error", "Error: "  +e.getMessage());					
					pool.shutdownNow();
				}
			}
		}catch (IOException ex) {
			String errorMessage = "Error: " + ex.getMessage();
			logger.addLine(TAG+errorMessage );
			sendMessage("error", errorMessage );			
			pool.shutdownNow();
		}
	}

	private ConnectionInstance getConnectionInstances(int id) {
		for (ConnectionInstance instance : connectionInstances) {
			if (id == instance.getId()){
				return instance;
			}
		}
		return null;		
	}

	private void deleteInstance(ConnectionInstance instance) {
		if (instance != null) {
			connectionInstances.remove(instance);
		}
	}

	private boolean sendMessageToServer(final String command) throws IOException {
		logger.addLine(TAG+ "Send command to server: "+ command);
		if (printWriter == null ){
			return false;
		}
		printWriter.println(command );		
		printWriter.println("END" );
		printWriter.flush();
		return true;		
	}

	private void receiveMessageFromServer() {	
		if (scanner == null ){
			return;
		}		
		StringBuffer buffer = new StringBuffer();
		while (scanner.hasNextLine()) {
			String readedLine = scanner.nextLine();
			if (readedLine.compareTo("END") !=  0) {
				buffer.append("+"+readedLine);
			}else{
				logger.addLine(TAG+ "Receive message from server: "+ buffer.toString());
				break;
			}
		}		
		if (parseServerAnswer(buffer.toString()) == false) {
			sendMessage("error", "Error: Server message parsing problem. Message: " + buffer.toString());
		}
	}

	private boolean parseServerAnswer(final String answer) {
		logger.addLine(TAG+ "Server input method: "+ answer);
		StringTokenizer token= new StringTokenizer(answer, "+");

		if  (!parseMethod(token.nextToken())) {
			return false;
		}
		// example: Header1: value1
		// Header2: value2		
		while (token.hasMoreTokens()) {
			String line = token.nextToken();
			if (line.trim().length() > 0) {
				System.out.println("Parse head "+ line);
				int separatorPosition = line.indexOf(':');
				if  (separatorPosition >= 0) {
					String type = line.substring(0,separatorPosition).trim().toUpperCase(Locale.getDefault());
					String value = line.substring(separatorPosition+1).trim();
					headerProperty.put(type, value);							
				}
			}
		}
		return true;		
	}

	private boolean parseMethod(final String inLine) {
		StringTokenizer stringTokens = new StringTokenizer(inLine);
		if (!stringTokens.hasMoreTokens()) {			
			System.out.println("Tokenized string is empty!");			
			return false;
		}		
		String version = stringTokens.nextToken();
		answerProperty.put("VERSION", version.toUpperCase(Locale.getDefault()));

		if ( !stringTokens.hasMoreTokens()){
			System.out.println("Tokenized string is too short!");			
			return false;
		}
		String code = stringTokens.nextToken();		
		answerProperty.put("CODE", code);

		if ( !stringTokens.hasMoreTokens()){
			System.out.println("Tokenized string is too short!");
			return false;
		}
		String text= stringTokens.nextToken();
		answerProperty.put("TEXT", text);
		return true;
	}


	/**
	 * GMT date formatter
	 */
	public static java.text.SimpleDateFormat gmtFormat;
	static	{
		gmtFormat = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
}
