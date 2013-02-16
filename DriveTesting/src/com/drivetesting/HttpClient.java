package com.drivetesting;

import http.filehandler.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TimeZone;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class HttpClient implements Runnable {
	private final int ServerPort = 4444;
	private  final String ServerAddress ="94.21.107.84";//192.168.0.2";//"10.0.2.2";
	private Logger logger;
	private Context context;
	final static Handler handler = new Handler();

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
	}

	public void run() {
		try {
			socket = new Socket(ServerAddress, ServerPort);
			portScanner = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());
			while (isRunning) {
				pingCommand();
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
			System.out.println("Get message from server");
			String readedLine = portScanner.nextLine();
			buffer.append(readedLine);
			while (readedLine.compareTo("END") !=  0) {
				readedLine = portScanner.nextLine();
				if (readedLine.compareTo("END") !=  0) {
					buffer.append("+"+readedLine);
				}			
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
