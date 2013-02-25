package http.filehandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

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
        try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPReceiver sender = new TCPReceiver(logger, 0);
        Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);     
	}
	
	@Test
	public void testTCPReceiverOnePacket() {
        final Socket socket = mock(Socket.class);
        String message = "asdfghjkl";
        String hash = Utility.calcCheckSum(message.getBytes());
        String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
        try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPReceiver sender = new TCPReceiver(logger, 0);
        Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);     
	}
	
	@Test
	public void testTCPReceiverTwoPacketWithSameID() {
        final Socket socket = mock(Socket.class);
        String message = "asdfghjkl";
        String hash = Utility.calcCheckSum(message.getBytes());
        String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+
        		"\nPOST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
        try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPReceiver sender = new TCPReceiver(logger, 0);
        Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==1);     
	}
	
	@Test
	public void testTCPReceiverTwoPacket() {
        final Socket socket = mock(Socket.class);
        String message = "asdfghjkl";
        String hash = Utility.calcCheckSum(message.getBytes());
        String str = new String("POST wtf.txt HTTP*/1.0\nID: 0\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+
        		"\nPOST wtf.txt HTTP*/1.0\nID: 1\nHASH: "+ hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\nEND\n");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
        try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPReceiver sender = new TCPReceiver(logger, 0);
        Assert.assertTrue("Message sent successfully", sender.readPackets(socket)==2);     
	}
}