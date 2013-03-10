package http.filehandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPReceiver extends Thread {
	private Logger logger = null;	
	private int id = 0;
	private FileInstance fileInstance = null;
	private DatagramSocket socket = null;	
	private int serverPort = 0;
	private String serverAddress = "";

	public UDPReceiver() {
		super();
		this.id = id;
		this.logger = logger;
	}

	public void run() {
		int PORT = 4000;
		byte[] buf = new byte[1000];
		DatagramPacket dgp = new DatagramPacket(buf, buf.length);
		try {
			socket = new DatagramSocket(PORT);

			System.out.println("Server started");
			while (true) {
				socket.receive(dgp);
				String rcvd = new String(dgp.getData(), 0, dgp.getLength()) + ", from address: "
						+ dgp.getAddress() + ", port: " + dgp.getPort();
				System.out.println(rcvd);
				
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}