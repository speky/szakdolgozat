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
        Assert.assertTrue("Message sent successfully", sender.call()==0);
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
        Assert.assertTrue("Message sent successfully", sender.call()==0);
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}

	@Test
	public void testTCPSendAFileInOnePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("asd".getBytes());        
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
        file.splitFileToPockets(30);
        sender.setFile(file);
        Assert.assertTrue("Message sent successfully", sender.call()==1);
        String message = "123456789asdfghjkyxcvbnm";
        String hash = Utility.calcCheckSum(message.getBytes());
        String testString = new String("POST test.txt HTTP*/1.0\nID: 0\nHASH: "+hash+"\nTEXT: "+message+"\n"+TCPSender.END_PACKET+"\r\nEND\r\n");
        String out = byteArrayOutputStream.toString(); 
        Assert.assertTrue(out.equals(testString));
	}

	@Test
	public void testTCPSendAFileInThreePacket() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("asd".getBytes());
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
        file.splitFileToPockets(10);
        sender.setFile(file);
        Assert.assertTrue("Message sent successfully", sender.call()==3);
        String message = "123456789a";        
        StringBuffer testString = new StringBuffer("POST test.txt HTTP*/1.0\nID: 0\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");
        
        message =  "sdfghjkyxc";
        testString.append("POST test.txt HTTP*/1.0\nID: 1\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");
        
        message = "vbnm";
        testString.append("POST test.txt HTTP*/1.0\nID: 2\nHASH: "+Utility.calcCheckSum(message.getBytes())+
        		"\nTEXT: "+message+"\nEND_PACKET\r\n");        
        testString.append("END\r\n");
        
        String out = byteArrayOutputStream.toString(); 
        Assert.assertTrue(out.equals(testString.toString()));
	}
}
