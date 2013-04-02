package http.filehandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TCPReceiverTest {
	private static Logger logger;

	@BeforeClass
	public static void prepare() {
		logger = new Logger("");		
	}

	@AfterClass
	public static void tearDown() {		
		logger.deleteLogFile();
	}

	@Test
	public void testTCPReceiverWithoutAnyInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==0);        
	}

	@Test
	public void testTCPReceiverEndMessage() {
		final Socket socket = mock(Socket.class);
		String str = new String("END\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==0);     
	}

	@Test
	public void testTCPReceiverOnePacketWithoutEnd() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		String hash = Utility.calcCheckSum(message.getBytes());
		String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP*/1.0\nACK: 0\nEND\n"));
	}

	@Test
	public void testTCPReceiverOnePacket() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		String hash = Utility.calcCheckSum(message.getBytes());
		String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP*/1.0\nACK: 0\nEND\n"));
	}

	@Test
	public void testTCPReceiverTwoPacketWithSameID() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		String hash = Utility.calcCheckSum(message.getBytes());
		String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+
				"\nPOST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP*/1.0\nACK: 0\nEND\n"));
	}

	@Test
	public void testTCPReceiverTwoPacket() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		String hash = Utility.calcCheckSum(message.getBytes());
		String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+
				"\nPOST wtf.txt HTTP*/1.0\nID: 1\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==2);
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP*/1.0\nACK: 0\nEND\n" +
				"POST wtf.txt HTTP*/1.0\nACK: 1\nEND\n"));
	}

	@Test
	public void testTCPReceiverOnePacketFrom5mbFile() {
		final Socket socket = mock(Socket.class);
		FileInstance instance = new FileInstance(logger, "5MB.bin");
		instance.splitFileToPockets(FileInstance.DEFAULT_SIZE);
		int pocketSize = instance.getPocketSize();
		Packet packet = instance.getPieces().get(0);
		String str = new String("POST 5MB.bin HTTP*/1.0\nID: 0\nHASH: "+ packet.hashCode+"\nTEXT: "+packet.text+"\n"+TCPSender.END_PACKET+"\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver sender = new TCPReceiver(logger, 0);
		Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST 5MB.bin HTTP*/1.0\nACK: 0\nEND\n"));
	}

	@Test
	public void testTCPReceiverAllPacketFrom5mbFile() {
		final Socket socket = mock(Socket.class);
		FileInstance instance = new FileInstance(logger, "5MB.bin");
		instance.splitFileToPockets(FileInstance.DEFAULT_SIZE);
		int packetSize = instance.getPocketSize();
		StringBuffer message = new StringBuffer("");
		StringBuffer ack = new StringBuffer("");
		List<Packet> packetList = instance.getPieces();
		int offset = 0;
		while (offset < packetSize) {
			int limit = 10 > packetSize? packetSize :10;        
			for (int i = offset; i < limit; ++i) {
				Packet packet = packetList.get(i);
				message.append("POST 5MB.bin HTTP*/1.0\nID:  "+i+"\nHASH: "+ packet.hashCode+"\nTEXT: "+packet.text+"\n"+TCPSender.END_PACKET+"\n");
				ack.append("POST 5MB.bin HTTP*/1.0\nACK: "+i+"\nEND\n");
				//System.out.println("id: "+ i +"buffer size: "+message.length());
			}
			message.append("END\n");
			String msg = message.toString();
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(msg.getBytes());
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				when(socket.getInputStream()).thenReturn(inputStream);
				when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			} catch (IOException e) {			
				e.printStackTrace();
			}

			TCPReceiver sender = new TCPReceiver(logger, 0);
			int receivedPacket = sender.readPackets(socket);
			Assert.assertTrue("Message sent successfully", receivedPacket==limit);
			Assert.assertTrue(byteArrayOutputStream.toString().equals(ack.toString()));
			offset += limit;
		}
	}

}
