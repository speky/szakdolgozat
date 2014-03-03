package http.testhandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class UDPReceiver extends ConnectionInstance {	
	private final String TAG = "UDP Receiver ";
	private final int reportInterval;
	private DatagramSocket socket = null;
	private InetAddress senderAddress = null;
	private int senderPort = 0;
	private final int bufferSize;
	private int readBytes = 0;	
	private double lastTransit = 0.0;
	private double jitter = 0.0;
	private int packetCount = 0;
	private int lost = 0;
	private int sum = 0;	
	private int cntOutofOrder = 0;
	private Timer timer = null;
	
	private MessageI reportSender = null;
	private ReceiverReportI reportReceiver = null;
	private boolean running = true;

	public UDPReceiver(final int id, Logger logger, MessageI sender, ReceiverReportI receiver, 	int reportInterval,	int bufferSize) {
		super(ConnectionInstance.UDP, id, logger);
		logger.addLine(TAG+ " id: " + id);
		reportSender = sender;
		reportReceiver = receiver;		
		this.reportInterval = reportInterval;		
		this.bufferSize = bufferSize;
	}

	// on the mobile side
	public boolean setSenderParameters(int port, String address) {		
		try {
			senderAddress  = InetAddress.getByName(address);
			senderPort = port;
			socket = new DatagramSocket();
			return true;
		} catch (UnknownHostException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		} catch (SocketException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}
		return false;
	}
	
	// on the server side
	public boolean setTestPort(int port) {
		try{	
			senderPort = port;
			socket = new DatagramSocket(port);
			return true;
		} catch (SocketException e) {
			logger.addLine(TAG+ " Error: " + e.getMessage());
		}
		return false;
	}
	
	@Override
	public void stop(){
		logger.addLine(TAG+" stopped!");
		running = false;
		if (socket != null) {
			socket.close();
			socket = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
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
		++sum;
	}

	// punch hole through the NAT from the mobile side
	private void sendAddressToSenderThroughNAT() {
		try{
			// Buffer for receiving incoming data
			byte[] inboundDatagramBuffer = new byte[bufferSize];
			DatagramPacket datagram = new DatagramPacket(inboundDatagramBuffer, inboundDatagramBuffer.length, senderAddress, senderPort);
			socket.send(datagram);
			DatagramPacket data = new DatagramPacket(inboundDatagramBuffer, bufferSize);
			socket.receive(data);
			//InetAddress adr = data.getAddress();
		} catch (IOException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}		
	}

	public Integer call() {

		if (senderAddress != null) {
				sendAddressToSenderThroughNAT();
		}

		//Declare reporter timer
		timer = new Timer();
		//Set the schedule function and rate
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (reportReceiver  != null ) {
					// print the interval in second
					reportReceiver.setReceivedBytes(id, reportInterval/1000, readBytes, jitter, lost, cntOutofOrder, sum);
				} else if (null != reportSender){
					reportSender.sendReportMessage(Integer.toString(id), "UDP", 
																	Integer.toString(id) + " "+
																	//print the interval in second
																	Integer.toString(reportInterval/1000) + " "+
																	Integer.toString(readBytes)+ " "+
																	 Double.toString(jitter)+ " "+
																	 Integer.toString(lost)+ " "+
																	 Integer.toString(cntOutofOrder)+" "+
																	 Integer.toString(sum)+ " ");
				}
				// reset read bytes
				readBytes = 0;
			}
		}, reportInterval, reportInterval);

		byte[] buf = new byte[bufferSize];
		DatagramPacket datagramPacket = new DatagramPacket(buf, bufferSize);		

		logger.addLine(TAG+"UDP receiver started");
		while (running && socket != null) {
			try {
				socket.receive(datagramPacket);
				byte[] bytes = datagramPacket.getData();
				readBytes += bytes.length;								
			} catch (IOException e) {
				logger.addLine(TAG+ " Error: " + e.getMessage());
				return id;
			}
			++packetCount;
			long time = Calendar.getInstance().getTimeInMillis();
			String received = new String(datagramPacket.getData(), 0, datagramPacket.getLength()) + ", from address: "
										+ datagramPacket.getAddress() + ", port: " + datagramPacket.getPort();
			//logger.addLine(TAG+received);
			parsePackage(received, time);

		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		logger.addLine(TAG+ "exited!");
		return id;
	}

}