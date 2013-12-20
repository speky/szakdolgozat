package http.testhandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import http.testhandler.Logger;
import http.testhandler.ReportSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReportSenderTest {
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
	public void testSendReportMessageTCP() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();        
        
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			
		} catch (IOException e) {	
			e.printStackTrace();
		}

        ReportSender sender = new ReportSender(logger, socket);
                        
        sender.sendReportMessage("1", "TCP",  "asd");							  
        Assert.assertTrue(byteArrayOutputStream.toString().equals("POST 1 HTTP*/1.0\nREPORT: TCP\nMESSAGE: asd\nEND\n"));
	}

	@Test
	public void testSendUDPReportMessage() {
        final Socket socket = mock(Socket.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();        
        
        try {
			when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
			
		} catch (IOException e) {	
			e.printStackTrace();
		}

        ReportSender sender = new ReportSender(logger, socket);
                        
        sender.sendReportMessage("2", "UDP",  "asd");
        Assert.assertTrue(byteArrayOutputStream.toString().equals("POST 2 HTTP*/1.0\nREPORT: UDP\nMESSAGE: asd\nEND\n"));
	}
}
