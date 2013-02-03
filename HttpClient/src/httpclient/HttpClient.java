package httpclient;

import http.filehandler.Logger;
import http.filehandler.Utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TimeZone;

class CommandThread {
	private Scanner scanner;
	private Socket socket;
	private Scanner portScanner;
	private PrintWriter pw;
	private Logger logger = new Logger("");
	private Properties answerProperty = new Properties();
	private Properties headerProperty = new Properties();
	
	public CommandThread() {
		super();		
		try {
			socket = new Socket(HttpClient.ServerAddress, HttpClient.ServerPort);
			portScanner = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());			
		}catch (UnknownHostException e) {
			logger.addLine("Unknown host exception, " + e.getMessage());
		} catch (IOException e) {
			logger.addLine("IOexception, " + e.getMessage());
		}
		scanner = new Scanner(System.in);
	}

	public void run() { 
		String command = "";
		while (true) {
			if  (scanner.hasNextLine()) {
				command = scanner.nextLine();
				if (command.length() != 0) {
					parseCommand(command);					
				}
			}
		}
	}

	private void parseCommand(final String command) {
		try {
			if (command.equals("getfiles")){
				System.out.println("getfiles parancs" );
				sendMessageToServer("getfiles");
				receiveMessageFromServer();

			}else if (command.equals("ping")) {
				System.out.println("ping parancs" );
				sendMessageToServer("PING / HTTP*/1.0");						
				receiveMessageFromServer();
				if (answerProperty.getProperty("CODE").equals("200") && answerProperty.getProperty("TEXT").equals("PONG")){
					logger.addLine("good answer from server, text:");
				}
			}
		}catch (IOException ex) {
			logger.addLine("Exception: "+ex.getMessage());
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
		if (portScanner.hasNextLine()) {
			logger.addLine("Get message from server");
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
		logger.addLine("Server input method: "+ answer);
		StringTokenizer token= new StringTokenizer(answer, "+");
		
		if  (!parseMethod(token.nextToken())) {
			return false;
		}
		// example: Header1: value1
		// Header2: value2		
		while (token.hasMoreTokens()) {
			String line = token.nextToken();
			if (line.trim().length() > 0) {
				logger.addLine("Parse head "+ line);
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
			logger.addLine("Tokenized string is empty!");			
			return false;
		}		
		String version = stringTokens.nextToken();
		answerProperty.put("VERSION", version.toUpperCase());

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine("Tokenized string is too short!");			
			return false;
		}
		String code = stringTokens.nextToken();		
		answerProperty.put("CODE", code);

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine("Tokenized string is too short!");
			return false;
		}
		String text= stringTokens.nextToken();
		answerProperty.put("TEXT", text);
		return true;
	}
}

public class HttpClient {
	public static  final int ServerPort = 13000;
	public static  final String ServerAddress = "localhost";

	public static void main(String[] args) {
		try	{
			// keyboard input scanner
			CommandThread command = new CommandThread();
			command.run();

			System.out.println("Client SHUT DOWN");
		} catch (Exception e) {
			System.out.println("thread hiba" + e.getMessage());
		}
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
