package com.drivetesting;

import http.filehandler.ICallback;
import http.filehandler.Logger;
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

public class HttpClient extends IntentService implements ICallback {
	public static final int MAX_THREAD = 10;
	public static final String PARAM_OWN_IP = "own_ip";
	public static final String PARAM_OUT_MSG = "out_msg";
	private static final int SOCKET_TIMEOUT = 5000;
	
	private final String TAG = "HttpClient: ";
	private final int ServerPort = 4444;
	private  final String serverAddress = "94.21.221.22";//"192.168.0.100"
	
	private Logger logger;
	private ExecutorService pool = null;
	private Set<Future<Integer>> threadSet = new HashSet<Future<Integer>>();
	private int threadCount = 0;
	private Socket socket;
	private Scanner scanner;
	private PrintWriter pw = null;
	private Properties answerProperty = new Properties();
	private Properties headerProperty = new Properties();
	private TCPReceiver receiver = null;
	private long mStartRX = 0;
	private long mStartTX = 0;
	private String ownIp = "";	
	private ReportTask task = new ReportTask();
	private int serverPort;
	private String errorMessage = null;
	
	class ReportTask extends TimerTask {
		public void run() {
			int packet = getReceivedPackets();
			System.out.println("** Packet: "+ packet);	

			Message m = new Message();			
			Bundle b = new Bundle();
			b.putInt("packet", packet); 
			m.setData(b);
			//handler.sendMessage(m);
		}
	}

	public  HttpClient () {
		super("HttpClientIntentService");				
		logger = new Logger("");
		logger.addLine(TAG+"test");
		pool = Executors.newFixedThreadPool(MAX_THREAD);
	}

	@Override
	public int setNumOfReceivedPackets(int packets) {
		return 0;
	}

	@Override
	public int setNumOfSentPackets(int packets) {
		return 0;
	}
	
	protected Socket createSocket(int port) {
		try {
			Socket socket = new Socket();			
			socket.connect(new InetSocketAddress(serverAddress, port), SOCKET_TIMEOUT);
			logger.addLine(TAG+" Create new socket");
			return socket;
		} catch (UnknownHostException e) {
			errorMessage  = "Socket creatin problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());			
		} catch (IOException e) {
			errorMessage = "Socket creatin problem";
			logger.addLine(TAG+"ERROR in run() " + e.getMessage());
		}
		return null;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		ownIp   = intent.getStringExtra(PARAM_OWN_IP);		
		//String resultTxt = msg + " " + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());

		try {
			socket = new Socket(serverAddress, ServerPort);
			scanner = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());

			/*	Message m = handler.obtainMessage(5, "ize");
			m.sendToTarget();
			/*new Message();			
			Bundle b = new Bundle();
			b.putInt("what", 5); // for example
			m.setData(b);
			handler.sendMessage(m);*/

			mStartRX = TrafficStats.getTotalRxBytes();
			mStartTX = TrafficStats.getTotalTxBytes();

			/*	if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {

			} else {
				mHandler.postDelayed(mRunnable, 1000);
			}
			 */

			makeNewThread();

			/*			m = new Message();
			b = new Bundle();
			b.putInt("end", 5);
			m.setData(b);
			handler.sendMessage(m);
			 */
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(HttpBroadcastReceiver.ACTION_RESP);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_OUT_MSG,  "blabal");
			sendBroadcast(broadcastIntent);			
		}catch (Exception e) {
			e.printStackTrace();
			pool.shutdownNow();
		}
	}

	private final Runnable mRunnable = new Runnable() {
		public void run() {
			long rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
			System.out.println(Long.toString(rxBytes));
			long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
			System.out.println(Long.toString(txBytes));
			//handler.postDelayed(mRunnable, 1000);
		}
	};

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

	public void pingCommand() {
		try {
			System.out.println("ping parancs" );
			sendMessageToServer("PING / HTTP*/1.0");						
			receiveMessageFromServer();
			if (answerProperty.getProperty("CODE").equals("200") && answerProperty.getProperty("TEXT").equals("PONG")){
				System.out.println("good answer from server, text:");
			}
		}catch (IOException ex) {
			System.out.println("Exception: "+ex.getMessage());
		}
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
			}
			logger.addLine(TAG+ "good answer from server");
			
			receiver = new TCPReceiver(logger, ++threadCount, this);
			receiver.setSocket(createSocket(testPort));
			Future<Integer> future = pool.submit(receiver);
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

			for (Future<Integer> futureInst : threadSet) {
				try {
					int value = futureInst.get();
					logger.addLine(TAG+"A thread ended, value: " + value);										
					timer.cancel();

					Message m = new Message();			
					Bundle b = new Bundle();

					if (value == -1) {
						b.putString("error", receiver.getErrorMEssage());
					} else {
						b.putInt("packet", getReceivedPackets());
					}
					m.setData(b);					
					//handler.sendMessage(m);
				} catch (ExecutionException e) {
					e.printStackTrace();
					pool.shutdownNow();
				} catch (InterruptedException e) {					
					e.printStackTrace();
					pool.shutdownNow();
				}
			}
		}catch (IOException ex) {
			logger.addLine(TAG+"Exception: "+ex.getMessage());
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
		parseServerAnswer(buffer.toString());
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

}
