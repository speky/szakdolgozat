package http.filehandler;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TCPSenderTest {
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
	public void testTCPSenderWithoutAnyParameter() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();        
        
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			
		} catch (IOException e) {	
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0, 0);
        PacketStructure packetStructure = new PacketStructure();
        packetStructure.receivedPackets = -1;        
        Assert.assertTrue(packetStructure.equals(sender.call()));
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}
	
	@Test
	public void testTCPSenderWithSocketParameter() {
        final Socket socket = mock(Socket.class);        
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {		
			e1.printStackTrace();
		}
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			when(socket.getInetAddress()).thenReturn(inetAddr);
			when(socket.getPort()).thenReturn(42);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0, 0);

        Assert.assertTrue(sender.setSocket(null) == false);
        Assert.assertTrue(sender.setSocket(socket) == true);
        PacketStructure packetStructure = new PacketStructure();
        packetStructure.receivedPackets = -1;
        //Assert.assertTrue(packetStructure.equals(sender.call()));
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}

	@Test
	public void testTCPSendAFileInOnePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String ackMessage = "POST test.txt HTTP*/1.0\n ACK: 0\nEND\n";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ackMessage.getBytes());        
        InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {		
			e1.printStackTrace();
		}
        try {
        	when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			when(socket.getInputStream()).thenReturn(byteArrayInputStream);
			when(socket.getInetAddress()).thenReturn(inetAddr);
			when(socket.getPort()).thenReturn(42);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0, 1);

        sender.setSocket(socket);               
        PacketStructure packetStructure = new PacketStructure();
        packetStructure.receivedPackets = 1;
        packetStructure.id = 0;
        //Assert.assertTrue(packetStructure.equals(sender.call()));
        String message = "123456789asdfghjkyxcvbnm";
        String hash = Utility.calcCheckSum(message.getBytes());
        String testString = new String("POST test.txt HTTP*/1.0\nID: 0\nHASH: "+hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\r\nEND\n\r\n");
        String out = byteArrayOutputStream.toString(); 
        //Assert.assertTrue(out.equals(testString));
	}

	@Test
	public void testTCPSendAFileInThreePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String ackMessage = "POST test.txt HTTP*/1.0\nACK: 0\nEND\nPOST test.txt HTTP*/1.0\nACK: 1\nEND\nPOST test.txt HTTP*/1.0\nACK: 2\nEND\n";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ackMessage.getBytes());
        InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {		
			e1.printStackTrace();
		}
        try {			
			when(socket.getInetAddress()).thenReturn(inetAddr);
			when(socket.getPort()).thenReturn(42);
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			when(socket.getInputStream()).thenReturn(byteArrayInputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0, 1);

        sender.setSocket(socket);
        
        PacketStructure packetStructure = new PacketStructure();
        packetStructure.receivedPackets = 3;
        packetStructure.id = 0;
        //Assert.assertTrue(packetStructure.equals(sender.call()));
        String message = "123456789a";        
        StringBuffer testString = new StringBuffer("POST test.txt HTTP*/1.0\nID: 0\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");
        
        message =  "sdfghjkyxc";
        testString.append("POST test.txt HTTP*/1.0\nID: 1\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");
        
        message = "vbnm";
        testString.append("POST test.txt HTTP*/1.0\nID: 2\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");        
        testString.append("END\n\r\n");
        
        String out = byteArrayOutputStream.toString(); 
        //Assert.assertTrue(out.equals(testString.toString()));
	}
}
