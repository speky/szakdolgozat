package http.filehandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class UDPReceiver extends ConnectionInstance {	
	private DatagramSocket socket = null;
	private Socket reportSocket;
	private final String TAG = "UDP Receiver ";
	private final int reportInterval;
	private final int port;
	private final int bufferSize;
	private int lastreport = 0;
	private double transferedData = 0.0;
	private double bandwidth = 0.0;
	private double jitter = 0.0;
	private int lost = 0;
	private int sum = 0;	
	private PrintWriter printWriter = null;	
	private UDPReportTask task = new UDPReportTask();
	
	class UDPReportTask extends TimerTask {
		public void run() {
			int preReport = lastreport;
			lastreport += reportInterval;
			String interval = Integer.toString(preReport) + "-" + Integer.toString(lastreport);
			try {
				sendReportToServer((new UDPReport(interval, transferedData, bandwidth, jitter, lost, sum)).toString());
			} catch (IOException ex) {
				logger.addLine(ex.getMessage());
			}
			transferedData = 0.0;
		}
	}

	private boolean sendReportToServer(final String command) throws IOException {
		logger.addLine(TAG+ "Send command to server: "+ command);
		if (reportSocket == null) {
			return false;
		}
		if (printWriter  == null ){
			printWriter = new PrintWriter(reportSocket.getOutputStream());
			return false;
		}
		printWriter .println(command );	
		printWriter .flush();
		return true;		
	}
	
	public UDPReceiver(final int id, Logger logger, Socket reportSocket, int reportInterval,	 int port, int bufferSize) {
		super(ConnectionInstance.UDP_RECEIVER, id, logger);	
		this.reportSocket = reportSocket;
		this.reportInterval = reportInterval;		
		this.port = port;
		this.bufferSize = bufferSize;
	}
	
	public void run() {		
		byte[] buf = new byte[bufferSize];
		DatagramPacket datagramPacket = new DatagramPacket(buf, bufferSize);
		//Declare reporter timer
		Timer timer = new Timer();
		//Set the schedule function and rate
		timer.scheduleAtFixedRate(
				task,
				//Set how long before to start calling the TimerTask (in milliseconds)
				3,
				//Set the amount of time between each execution (in milliseconds)
				reportInterval);
		try {
			socket = new DatagramSocket(port);	
			logger.addLine(TAG+"UDP receiver started");
			while (true) {
				socket.receive(datagramPacket);
				String received = new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + ", from address: "
						+ datagramPacket.getAddress() + ", port: " + datagramPacket.getPort();
				logger.addLine(TAG+received);
				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}