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
	public void testTCPReceiverWithEnd() {
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

}