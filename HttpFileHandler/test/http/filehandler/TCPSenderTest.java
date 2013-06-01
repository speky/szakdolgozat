package http.filehandler;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

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

        TCPSender sender = new TCPSender(logger, 0) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };
        Assert.assertTrue(sender.call()==-1);
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}
	
	@Test
	public void testTCPSenderWithSocketParameter() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };

        sender.setReceiverParameters(42, "1.1.1.1");
        Assert.assertTrue("Message sent successfully", sender.call()==-1);
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}

	@Test
	public void testTCPSendAFileInOnePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String ackMessage = "POST test.txt HTTP*/1.0\n ACK: 0\nEND\n";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ackMessage.getBytes());        
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			when(socket.getInputStream()).thenReturn(byteArrayInputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };

        sender.setReceiverParameters(42, "1.1.1.1");
        FileInstance file =  new FileInstance(logger, "test.txt");
        file.splitFileToPackets(30);
        sender.setFile(file);
        Assert.assertTrue("Message sent successfully", sender.call()==0);
        String message = "123456789asdfghjkyxcvbnm";
        String hash = Utility.calcCheckSum(message.getBytes());
        String testString = new String("POST test.txt HTTP*/1.0\nID: 0\nHASH: "+hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\r\nEND\n\r\n");
        String out = byteArrayOutputStream.toString(); 
        Assert.assertTrue(out.equals(testString));
	}

	@Test
	public void testTCPSendAFileInThreePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String ackMessage = "POST test.txt HTTP*/1.0\nACK: 0\nEND\nPOST test.txt HTTP*/1.0\nACK: 1\nEND\nPOST test.txt HTTP*/1.0\nACK: 2\nEND\n";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ackMessage.getBytes());
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			when(socket.getInputStream()).thenReturn(byteArrayInputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

        TCPSender sender = new TCPSender(logger, 0) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };

        sender.setReceiverParameters(42, "1.1.1.1");
        FileInstance file =  new FileInstance(logger, "test.txt");
        file.splitFileToPackets(10);
        sender.setFile(file);
        Assert.assertTrue("Message sent successfully", sender.call()==0);
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
        Assert.assertTrue(out.equals(testString.toString()));
	}
}
