package http.testhandler;

import static org.mockito.Mockito.*;

import http.testhandler.Logger;
import http.testhandler.TCPSender;

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
                        
        Assert.assertTrue(0 == sender.call());
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
        
        //Assert.assertTrue(packetStructure.equals(sender.call()));
        Assert.assertTrue(byteArrayOutputStream.toString().equals(""));
	}

}
