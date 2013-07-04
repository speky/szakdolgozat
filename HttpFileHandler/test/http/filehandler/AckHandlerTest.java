package http.filehandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AckHandlerTest {

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
	public void testAckHandlerWithoutAnyInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		HashSet<Integer> set = new HashSet<Integer> ();
		AckHandler handler = new AckHandler(logger);
		handler.startAckReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerWithoutNewLineEnd() {
		final Socket socket = mock(Socket.class);
		String str = new String("END");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		HashSet<Integer> set = new HashSet<Integer> ();
		AckHandler handler = new AckHandler(logger);
		handler.startAckReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerWithWrongInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST / HTTP*/1.0\nAC: 1");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		HashSet<Integer> set = new HashSet<Integer> ();
		AckHandler handler = new AckHandler(logger);
		handler.startAckReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerGoodInputWithoutEnd() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST / HTTP*/1.0\nACK: 1\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		HashSet<Integer> set = new HashSet<Integer> ();
		AckHandler handler = new AckHandler(logger);
		handler.startAckReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerGoodInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST fileName HTTP*/1.0\nACK: 1\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		HashSet<Integer> set = new HashSet<Integer> ();
		AckHandler handler = new AckHandler(logger);
		handler.startAckReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==1);
		Assert.assertTrue("", set.contains(0)==false);
		Assert.assertTrue("", set.contains(1)==true);
	}

	@Test
	public void testAckSend() {		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		AckHandler handler = new AckHandler (logger);           
		handler.sendAckMessage(byteArrayOutputStream, "fileName", 1);

		String testString = new String("POST fileName HTTP*/1.0\nACK: 1\n"+"END\n");
		String out = byteArrayOutputStream.toString(); 
		Assert.assertTrue(out.equals(testString));
	}

}
