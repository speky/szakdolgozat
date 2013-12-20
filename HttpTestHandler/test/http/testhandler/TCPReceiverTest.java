package http.testhandler;

import http.testhandler.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TCPReceiverTest{
	private static Logger logger;

	@BeforeClass
	public static void prepare() {
		logger = new Logger("");
	}
	
	@AfterClass
	public static void tearDown() {
		logger.deleteLogFile();
	}
/*	
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

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver.stop();
		Assert.assertTrue(receiver.getReceivedPacket() == 0);
		Assert.assertTrue(receiver.getSentPacket() == 0);
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

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver.stop();
		Assert.assertTrue(receiver.getReceivedPacket() == 0);
		Assert.assertTrue(receiver.getSentPacket() == 0);
	}

	@Test
	public void testTCPReceiverOnePacketWithoutEnd() {
		final Socket socket = mock(Socket.class);
		String message = "asd";
				
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(message.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver.stop();
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP* /1.0\nREPORT: 3\nEND\n"));
		Assert.assertTrue(receiver.getReceivedPacket() == 3);
		Assert.assertTrue(receiver.getSentPacket() == 0);
	}

	@Test
	public void testTCPReceiverOnePacket() {
		final Socket socket = mock(Socket.class);
		String str = "asdfghjkl";
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP* /1.0\nACK: 0\nEND\n"));
		Assert.assertTrue(receiver.getReceivedPacket() == 1);
		Assert.assertTrue(receiver.getSentPacket() == 1);
	}

	@Test
	public void testTCPReceiverTwoPacketWithSameID() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		Assert.assertTrue(byteArrayOutputStream.toString().equals("POST wtf.txt HTTP* /1.0\nACK: 0\nEND\nPOST wtf.txt HTTP* 1.0\nACK: 0\nEND\n"));
		Assert.assertTrue(receiver.getReceivedPacket() == 2);
		Assert.assertTrue(receiver.getSentPacket() == 2);
	}

	@Test
	public void testTCPReceiverTwoPacket() {
		final Socket socket = mock(Socket.class);
		String message = "asdfghjkl";
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		TCPReceiver receiver = new TCPReceiver(logger, "0");
		receiver.setSocket(socket);
		receiver.readPackets();
		Assert.assertTrue(receiver.getReceivedPacket() == 2);
		Assert.assertTrue(receiver.getSentPacket() == 2);
	}

*/
	
	/*@Test
	public void testTCPReceiverAllPacketFrom5mbFile() {
		final Socket socket = mock(Socket.class);
		FileInstance instance = new FileInstance(logger, "5MB.bin");
		instance.splitFileToPackets(FileInstance.DEFAULT_SIZE);
		int packetSize = instance.getPocketSize();
		StringBuffer message = new StringBuffer("");
		StringBuffer ack = new StringBuffer("");
		List<Packet> packetList = instance.getPieces();
		int offset = 0;
		while (offset < packetSize) {
			int limit = 10 > packetSize? packetSize :10;        
			for (int i = offset; i < limit; ++i) {
				Packet packet = packetList.get(i);
				message.append("POST 5MB.bin HTTP* /1.0\nID:  "+i+"\nHASH: "+ packet.hashCode+"\nTEXT: "+packet.text+"\n"+TCPSender.END_PACKET+"\n");
				ack.append("POST 5MB.bin HTTP* /1.0\nACK: "+i+"\nEND\n");
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
	*/

}
