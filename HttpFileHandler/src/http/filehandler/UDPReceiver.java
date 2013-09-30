package http.filehandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;

public class UDPReceiver extends ConnectionInstance {	
	private DatagramSocket socket = null;
	private Socket reportSocket;
	private final String TAG = "UDP Receiver ";
	private final int reportInterval;
	private final int port;
	private final int bufferSize;
	private int lastreport = 0;
	private double transferedData = 0.0;
	private int totalReadedBytes = 0;
	private double bandwidth = 0.0;
	private double lastTransit = 0.0;
	private double jitter = 0.0;
	private int lost = 0;
	private int sum = 0;
	private int packetCount = 0;
	private int cntOutofOrder = 0;
	private MessageI reportSender = null;
	private ReceiverReportI reportReceiver = null;
	
	public UDPReceiver(final int id, Logger logger, MessageI sender, ReceiverReportI receiver, Socket reportSocket, 
			int reportInterval,	 int port, int bufferSize) {
		super(ConnectionInstance.UDP, id, logger);
		reportSender = sender;
		reportReceiver = receiver;
		this.reportSocket = reportSocket;
		this.reportInterval = reportInterval;		
		this.port = port;
		this.bufferSize = bufferSize;
	}
	
	private void parsePackage(String packet, long receivedTime) {
		StringTokenizer tokens = new StringTokenizer(packet);		
		int stringPacketId = Integer.parseInt((String)tokens.nextElement());
		long sentTime = Long.parseLong((String)tokens.nextElement());
		 // from RFC 1889, Real Time Protocol (RTP) 
        // J = J + ( | D(i-1,i) | - J ) / 16 
		double transit = receivedTime - sentTime;
        if (lastTransit != 0.0) {
        	double deltaTransit = Math.abs(transit - lastTransit);
            jitter += (deltaTransit - jitter) / (16.0);
        }
        lastTransit = transit;

        // packet loss occured if the datagram numbers aren't sequential 
        if (stringPacketId != packetCount) {
            if (stringPacketId <  packetCount) {
                ++cntOutofOrder;
            } else {
                lost += stringPacketId - packetCount - 2;
            }
        }
        // never decrease datagramID (e.g. if we get an out-of-order packet) 
        if ( stringPacketId  > packetCount) {
        	packetCount = stringPacketId;
        }
	}
	
	public void run() {
		//Declare reporter timer
		Timer timer = new Timer();
		//Set the schedule function and rate
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
				  if (reportReceiver  != null ) {								
					  reportReceiver.setReceivedBytes(reportInterval, totalReadedBytes);
				  } else if (null != reportSender){
					  reportSender.sendReportMessage(Integer.toString(id), "UDP", Integer.toString(totalReadedBytes));
				  }
			  }
			}, reportInterval, reportInterval);
		
		byte[] buf = new byte[bufferSize];
		DatagramPacket datagramPacket = new DatagramPacket(buf, bufferSize);		
		try {
			socket = new DatagramSocket(port);	
			logger.addLine(TAG+"UDP receiver started");
			while (true) {
				socket.receive(datagramPacket);
				++packetCount;
				long time = Calendar.getInstance().getTimeInMillis();
				String received = new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + ", from address: "
						+ datagramPacket.getAddress() + ", port: " + datagramPacket.getPort();
				logger.addLine(TAG+received);
				parsePackage(received, time);
				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}