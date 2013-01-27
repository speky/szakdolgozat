package httpclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

class CPeer {
	public Socket socket;
	public DataOutputStream outputStream;
	public Scanner scanner;
	public int id;
	public int port;
	public HashSet<String> files;

	CPeer(final Socket socket, final int id, final int port) {
		try{
			this.socket = socket;
			this.id = id;
			this.port = port;			
			outputStream = new DataOutputStream(socket.getOutputStream());
			scanner = new Scanner(socket.getInputStream());
			files = new HashSet<String>();
		}
		catch(Exception e){
			System.out.println("peer hiba: " + e.getMessage() + " Peer id " + id);
		}
	}
}

class PeerThread implements Runnable {
	private CPeer peer;

	public PeerThread(Socket s, int id) {
		super();
		// regiser a new peer
		peer = new CPeer(s, id, 0);        
	}

	public void run()  {
		try  {
			while (true) {
				if (peer.scanner.hasNextLine()) {
					String str = peer.scanner.nextLine();
					if (str.length() != 0) {
						List<String> order = new ArrayList<String>();
						order = ((List<String>)Arrays.asList(str.split(" ")));
						// peer parancsok
						if (order.get(0).equals("ping")){
							peer.outputStream.writeChars("pong");
							peer.outputStream.flush();

						} else{ // hibás parancs
							System.out.println("hibás parancs, bontjuk a kapcsoaltot. (Peer id " + peer.id+" )");
							peer.socket.close();
							peer.outputStream.close();
							peer.scanner.close();
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("run hiba " + e.getMessage());            
		}
	}
}

class CommandThread {
	private Scanner scanner;
	private Socket socket;
	private Scanner portScener ;
	private PrintWriter pw;

	public CommandThread(){
		super();
		try {
			socket = new Socket(HttpClient.ServerAddress, HttpClient.ServerPort);
			portScener = new Scanner(socket.getInputStream());
			pw = new PrintWriter(socket.getOutputStream());
			
		}catch (UnknownHostException e) {
			System.out.println("unknown host exception, " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOexception, " + e.getMessage());
		}
		scanner = new Scanner(System.in);
	}

	public void run() throws IOException { 
		String command = "";
		while (true) {
			if  (scanner.hasNextLine()) {
				command = scanner.nextLine();
				if (command.length() != 0) {
					//  parancsok feldolgozasa
					if (command.equals("getfiles")){
						System.out.println("getfiles parancs" );
						sendMessageToServer("getfiles");
						receiveMessageFromServer();
						
					}else if (command.equals("ping")) {
						System.out.println("ping parancs" );
						sendMessageToServer("PING / HTTP*/1.0");						
						receiveMessageFromServer();
					}
				}
			}
		}
	}

	private boolean sendMessageToServer(final String command) throws IOException {
		System.out.println("Send command to server: "+ command);
		pw.println(command );		
		pw.println("END" );
		pw.flush();

		StringBuffer buffer = new StringBuffer();
		while (portScener .hasNextLine()) {
			String str = portScener .nextLine();			
			buffer.append(str);
			System.out.println("Received line:" +str);
		}
		return true;		
	}

	private String receiveMessageFromServer() {		
		StringBuffer buffer = new StringBuffer();
		while (portScener .hasNextLine()) {
			String str = portScener .nextLine();			
			buffer.append(str);
			System.out.println("Received line:" +str);
		}
		return buffer.toString();
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
