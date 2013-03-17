package com.drivetesting;

import http.filehandler.FileInstance;
import http.filehandler.Logger;
import http.filehandler.TCPReceiver;
import http.filehandler.TCPSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class HttpClient implements Runnable {
	private final int ServerPort = 4444;
	private  final String ServerAddress ="10.0.2.15";//92.249.224.209";//192.168.0.2";//"10.0.2.2";
	private Logger logger;
	private Context context;
	final static Handler handler = new Handler();
	private ExecutorService pool = null;
	private Set<Future<Integer>> threadSet = new HashSet<Future<Integer>>();
	private int threadCount = 0;
	static final int MAX_THREAD = 10;
	private boolean isRunning;
	private Socket socket;
	private Scanner portScanner;
	private PrintWriter pw;
	private Properties answerProperty = new Properties();
	private Properties headerProperty = new Properties();

	public  HttpClient (Context context) {
		this.context = context;			
		isRunning = true;
		logger = new Logger("");
		logger.addLine("test");
		pool = Executors.newFixedThreadPool(MAX_THREAD);
	}

	public void run() {
		try {
			socket = new Socket(ServerAddress, ServerPort);
			portScanner = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());

			while (isRunning) {
				makeNewThread();
				handler.post(new Runnable() {
					@Override public void run() {
						hint(context, "new client connected");
					}});
				isRunning = false;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						String name = inetAddress.getHostAddress().toString();
						return name;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("ex getLocalIpAddress", ex.toString());
		}
		return null;
	}

	private static void hint(final Context mycontext, final String s) {
		Toast toast=Toast.makeText(mycontext, s, Toast.LENGTH_SHORT);
		toast.show();
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

	public void makeNewThread() {
		try {
			System.out.println("makeNewThread" );
			System.out.println("IP: " +getLocalIpAddress());
			sendMessageToServer("GET /5MB.bin HTTP* /1.0\nPORT: 44450\nDATE: 2013.03.03\nMODE: DL\n CONNECTION: TCP\n");					receiveMessageFromServer();
			if (answerProperty.getProperty("CODE").equals("200") && answerProperty.getProperty("TEXT").equals("OK")){
				System.out.println("good answer from server, text:");
			}

			{
				TCPReceiver receiver = new TCPReceiver(logger, threadCount++);				
				receiver.setSenderParameters(44450);
				Future<Integer> future = pool.submit(receiver);
				threadSet.add(future);
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
		}catch (IOException ex) {
			System.out.println("Exception: "+ex.getMessage());
		}
	}

	private boolean sendMessageToServer(final String command) throws IOException {
		System.out.println("Send command to server: "+ command);
		pw.println(command );		
		pw.println("END" );
		pw.flush();
		return true;		
	}

	private void receiveMessageFromServer() {		
		StringBuffer buffer = new StringBuffer();
		while (portScanner.hasNextLine()) {			
			String readedLine = portScanner.nextLine();
			if (readedLine.compareTo("END") !=  0) {
				buffer.append("+"+readedLine);
			}else{
				break;
			}

		}		
		parseServerAnswer(buffer.toString());
	}

	private boolean parseServerAnswer(final String answer) {
		System.out.println("Server input method: "+ answer);
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
