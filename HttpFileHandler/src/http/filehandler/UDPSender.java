package http.filehandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

class UDPSender extends Thread {
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private DatagramSocket socket = null;	
	private int serverPort = 0;
	private String serverAddress = "";

	public UDPSender(Logger logger, final int id) {
		super();
		this.id = id;
		this.logger = logger;
		//logger.addLine("Add a client, id: " + client.id + " IP: " + socket.getInetAddress().getHostAddress());
	}

	public void setReceiverParameters(final int port, final String address) {
		serverAddress = address;
		serverPort = port;		
	}

	public void setFile(final FileInstance instance) {
		fileInstance = instance;
	}

	public void run() {
		try {
			socket = new DatagramSocket();
			byte[] buf = new byte[1000];
			//DatagramPacket dp = new DatagramPacket(buf, buf.length);
			InetAddress hostAddress = InetAddress.getByName("localhost");
			while (true) {

				String outMessage = " "; //stdin.readLine();

				//if (outMessage.equals("bye"))
				//break;
				String outString = "Client say: " + outMessage;
				buf = outString.getBytes();

				DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, 9999);
				socket.send(out);

				/*s.receive(dp);
				String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ": "
						+ new String(dp.getData(), 0, dp.getLength());				
				System.out.println(rcvd);
				 */
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
