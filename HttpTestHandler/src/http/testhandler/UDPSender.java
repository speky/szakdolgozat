package http.testhandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

public class UDPSender extends ConnectionInstance {
	private DatagramSocket socket = null;	
	private int receiverPort = 0;
	private InetAddress receiverAddress = null;
	private int bufferSize;	//in Byte
	private final String TAG = "UDPSender: ";
	private double UDPRate = 1024.0 * 1024.0; //1MBit/sec
	private final int Bytes_to_Bits = 8;  
	private int packetID;
	private long adjust ;
	private double delay_target ;
	private long delay; 
	private boolean running = true;

	public UDPSender(final int id, Logger logger, final int bufferSizeInByte) {
		super(ConnectionInstance.UDP, id, logger);
		logger.addLine(TAG+ " id: " + id);
		bufferSize = bufferSizeInByte;
		initDelay();
	}

	public void setRateInBitsPerSec(final double rate) {
		UDPRate = rate;
		initDelay();
	}

	private void initDelay() {
		// Due to the UDP timestamps etc, included 
		// reduce the read size by an amount 
		// equal to the header size
		delay_target = 0.0;
		delay = 0;
		adjust = 0;
		// compute delay for bandwidth restriction, constrained to [0,1] seconds
		// 1 / (bits per second / bits) = second 
		delay_target = 1.0 / (double)(UDPRate/ (bufferSize * Bytes_to_Bits));
		// store in milliseconds
		delay_target *= 1000.0;
		logger.addLine(TAG + "DELAY_TARGET: "+ delay_target );

	}

	// used on mobile side
	public boolean setReceiverParameters(final int port, final String address) {
		receiverPort = port;
		try {
			receiverAddress  = InetAddress.getByName(address);
			receiverPort = port;
			socket = new DatagramSocket();
			return true;
		} catch (UnknownHostException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		} catch (SocketException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}
		return false;
	}

	// used on server side
	public boolean setReceiverParameter(final int port) {
		receiverAddress = null;
		receiverPort = port;
		try {
			socket = new DatagramSocket(port);
			return true;
		} catch (SocketException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}
		return false;
	}

	private void getAddressThroughNAT() {
		// Buffer for receiving incoming data
		byte[] inboundDatagramBuffer = new byte[bufferSize];
		DatagramPacket inboundDatagram = new DatagramPacket(inboundDatagramBuffer, inboundDatagramBuffer.length);
		// Actually receive the datagram
		try {
			socket.receive(inboundDatagram);
			// Source IP address
			receiverAddress = inboundDatagram.getAddress();
			receiverPort = inboundDatagram.getPort();
			DatagramPacket out = new DatagramPacket(inboundDatagramBuffer, 1000, receiverAddress, receiverPort);
			socket.send(out);
		} catch (IOException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void stop() {
		logger.addLine(TAG+" stopped!");
		running = false;

	}

	public Integer call() {
		try {
			byte[] buf = new byte[bufferSize];
			packetID = 0;
			long lastPacketTime = 0;
			int packetSize = 0;

			if (receiverAddress == null) {
				getAddressThroughNAT();
			}

			lastPacketTime = Calendar.getInstance().getTimeInMillis();
			while (running) {
				long time = Calendar.getInstance().getTimeInMillis();
				// delay between writes
				// make an adjustment for how long the last loop iteration took
				adjust = (long)(delay_target) + (lastPacketTime-time);
				logger.addLine(TAG + "ADJUST: "+ adjust);
				lastPacketTime = time;

				if (adjust > 0  ||  delay > 0) {
					delay += adjust;
				}
				logger.addLine(TAG + "DELAY: "+ delay);

				byte[]  packetData = (Integer.toString(++packetID) +" " + Long.toString(time) +" ").getBytes();
				// re-generate byte buffer array if the packet id or time data's length has changed
				if (packetSize != packetData.length) {
					Utility.fillStringBuffer(buf, bufferSize, packetData);
				}

				DatagramPacket out = new DatagramPacket(buf, buf.length, receiverAddress, receiverPort);
				socket.send(out);

				// wait for hold the preset 
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
						return id;
					}
				}
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (SocketException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		} catch (UnknownHostException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		} catch (IOException e) {
			logger.addLine(TAG+ " Error: " + e.getLocalizedMessage());
		}
		logger.addLine(TAG+ "exited!");
		return id;
	}
}
