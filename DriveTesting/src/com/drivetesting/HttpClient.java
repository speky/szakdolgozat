package com.drivetesting;

import http.filehandler.Logger;
import http.filehandler.PacketStructure;
import http.filehandler.TCPReceiver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.IntentService;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class HttpClient extends IntentService {
	public static final int MAX_THREAD = 10;
	public static final String PARAM_OWN_IP = "own_ip";
	public static final String PARAM_OUT_MSG = "out_msg";
	public static final String PARAM_PACKET = "packet";
	private static final int SOCKET_TIMEOUT = 3000;

	private static final double BYTE_TO_KILOBIT = 0.0078125;
	private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

	private final String TAG = "HttpClient: ";
	private final int ServerPort = 4444;
	private  String serverAddress = null;
	private Logger logger;
	private ExecutorService pool = null;
	private Set<Future<PacketStructure>> threadSet = new HashSet<Future<PacketStructure>>();
	private int threadCount = 0;
	private Socket socket;
	private Scanner scanner;
	private PrintWriter pw = null;
	private Properties answerProperty = new Properties();
	private Properties headerProperty = new Properties();
	private TCPReceiver receiver = null;
	
	private ReportTask task = new ReportTask();	
	private String errorMessage = null;
	private Messenger messenger = null;
	
	private long previousRxBytes = 0;
	private long previousTxBytes = 0;
	private long time = 0;
	private SpeedInfo downloadSpeed;
	private SpeedInfo uploadSpeed;
	
	class ReportTask extends TimerTask {
		public void run() {
			int packet = getReceivedPackets();
			int sentPacket = getSentPackets();
			calcSpeed();
			logger.addLine("** Packet: "+ packet + " sent: "+sentPacket);	
			sendMessage("packet", /*"Packet: "+Integer.toString(packet)+ " sent: "+Integer.toString(sentPacket) +*/
				downloadSpeed.getSpeedString() + " " + uploadSpeed.getSpeedString());
		}
	}

	private void sendMessage(final String key, final String value) {
		if (messenger != null) {
			Message m = Message.obtain();			
			Bundle b = new Bundle();
			b.putString(key, value); 
			m.setData(b);
			try {
				Log.d("Client", key + " "+ value);
				messenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public  HttpClient() {
		super("HttpClientIntentService");				
		logger = new Logger("");
		logger.addLine(TAG+"test");
		pool = Executors.newFixedThreadPool(MAX_THREAD);
	}
	    
    protected Socket createSocket(int port) {
		try {
			Socket socket = new Socket();			
			socket.connect(new InetSocketAddress(serverAddress, port), SOCKET_TIMEOUT);
			logger.addLine(TAG+" Create new socket");
			return socket;
		} catch (UnknownHostException e) {
			errorMessage  = "Test socket creating problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			sendMessage("error", errorMessage);			
		} catch (IOException e) {
			errorMessage = "Test socket creating problem (I/O)";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
			sendMessage("error", errorMessage);			
		}
		return null;
	}

	private void calcSpeed() {
		int i = android.os.Process.myUid();
		int j = getApplication().getApplicationInfo().uid;
		
		long rxBytes = TrafficStats.getMobileRxBytes() - previousRxBytes; //getUidRxBytes(android.os.Process.myUid()) - previousRxBytes;
		System.out.println("Rx bytes: "+Long.toString(rxBytes));
		long txBytes = TrafficStats.getUidTxBytes(android.os.Process.myUid()) - previousTxBytes;
		System.out.println("Tx bytes: "+Long.toString(txBytes));

		long currentTime =  System.currentTimeMillis();
		long ellapsedTime = currentTime - time;
		time = currentTime;

		downloadSpeed = calculate(ellapsedTime, rxBytes);
		uploadSpeed = calculate(ellapsedTime, txBytes);

		previousTxBytes = txBytes;
		previousRxBytes = rxBytes;			
	}

	/**
	 * 1 byte = 0.0078125 kilobits
	 * 1 kilobits = 0.0009765625 megabit
	 * 
	 * @param time in miliseconds
	 * @param bytes number of bytes downloaded/uploaded
	 * @return SpeedInfo containing current speed
	 */
	private SpeedInfo calculate(final long time, final long bytes){
		SpeedInfo info = new SpeedInfo();
		if (time == 0) {
			return info;
		}
		info.bps = (bytes / (time / 1000.0) );
		info.kilobits  = info.bps  * BYTE_TO_KILOBIT;
		info.megabits = info.kilobits * KILOBIT_TO_MEGABIT;
		
		return info;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		messenger = (Messenger) intent.getExtras().get("handler"); 		
		//String resultTxt = msg + " " + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());

		serverAddress = (String)intent.getExtras().get("serverIp");
		if (serverAddress == null || serverAddress.equals("0.0.0.0")) {
			sendMessage("error", "Error: Invalid server address!");
			System.out.println("Invalid server address!");
		}
		
		try {
			socket = new Socket(serverAddress, ServerPort);
			scanner = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());

			previousRxBytes = TrafficStats.getMobileRxBytes();//(android.os.Process.myUid());
			previousTxBytes = TrafficStats.getUidTxBytes(android.os.Process.myUid());
			time =System.currentTimeMillis();
			
			if (previousTxBytes  == TrafficStats.UNSUPPORTED || previousTxBytes == TrafficStats.UNSUPPORTED) {
				System.out.println("Traffic stat not supported! ");
			}

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

	public int getReceivedPackets() {
		if (receiver != null) {
			return receiver.getReceivedPacket();
		}
		return 0;
	}

	public int getSentPackets() {
		if (receiver != null) {
			return receiver.getSentPacket();
		}
		return 0;
	}

	public void stop() {
		logger.addLine(TAG+ "send stop to server");
		try {
			sendMessageToServer("STOP / Http*/1.0\n DATE:2013.12.12\nCONNECTION: STOP\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addLine(TAG+ "stop threads");

		if (receiver != null) {
			receiver.stop();
		}		
		pool.shutdownNow();
	}

	public void makeNewThread() {
		try {
			logger.addLine(TAG+"makeNewThread" );			
			sendMessageToServer("GET /5MB.bin HTTP*/1.0\nDATE: 2013.03.03\nMODE: DL\n CONNECTION: TCP\n");					

			receiveMessageFromServer();
			int testPort = Integer.parseInt(headerProperty.getProperty("PORT")); 
			if (!answerProperty.getProperty("CODE").equals("200") && answerProperty.getProperty("TEXT").equals("OK")) {
				logger.addLine(TAG+ "Bad answer from server, text:"+answerProperty.getProperty("TEXT"));
				sendMessage("error", "Server reject the test: "+ answerProperty.getProperty("TEXT"));				
			}
			logger.addLine(TAG+ "good answer from server");

			Socket socket = createSocket(testPort);
			if (socket == null) {
				sendMessage("error", "Could not connect to server! test port: "+ testPort);
				return;
			}
			
			calcSpeed();
			
			receiver = new TCPReceiver(logger, ++threadCount);
			receiver.setSocket(socket);
			Future<PacketStructure> future = pool.submit(receiver);
			threadSet.add(future);

			//Declare the timer
			Timer timer = new Timer();
			//Set the schedule function and rate
			timer.scheduleAtFixedRate(
					task,
					//Set how long before to start calling the TimerTask (in milliseconds)
					10,
					//Set the amount of time between each execution (in milliseconds)
					1000);

			for (Future<PacketStructure> futureInst : threadSet) {
				try {
					PacketStructure value = futureInst.get();
					logger.addLine(TAG+"A thread ended, value: " + value);										
					timer.cancel();
					calcSpeed();
					if (value.sentPackets == -1 || value.receivedPackets == -1) {
						sendMessage("error", "Error: " + receiver.getErrorMessage());
					} else {
						sendMessage("end", "Test end, received packets: "+ getReceivedPackets());
					}						
				} catch (ExecutionException e) {
					e.printStackTrace();
					sendMessage("error", "Error: "+e.getMessage());					
					pool.shutdownNow();
				} catch (InterruptedException e) {					
					e.printStackTrace();
					sendMessage("error", "Error: "+e.getMessage());					
					pool.shutdownNow();
				}
			}
		}catch (IOException ex) {
			String errorMessage = "Error: "+ex.getMessage();
			logger.addLine(TAG+errorMessage );
			sendMessage("error", errorMessage );			
			pool.shutdownNow();
		}
	}

	private boolean sendMessageToServer(final String command) throws IOException {
		logger.addLine(TAG+ "Send command to server: "+ command);
		if (pw == null ){
			return false;
		}
		pw.println(command );		
		pw.println("END" );
		pw.flush();
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
					String type = line.substring(0,separatorPosition).trim().toUpperCase();
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
		answerProperty.put("VERSION", version.toUpperCase());

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

	private static class SpeedInfo{
		public double kilobits=0;
		public double megabits=0;
		public double bps=0;

		public String getSpeedString() {
			if (megabits > 1.0) {
				return "speed: "+ Double.toString(bps)+ " bps" +" Current speed: " + Double.toString(megabits)+ " Mbit/s";
			} else {
				return "speed: "+ Double.toString(bps)+ " bps" +" Current speed: " + Double.toString(kilobits) + " Kbit/s";
			}		
		}
	}

}
