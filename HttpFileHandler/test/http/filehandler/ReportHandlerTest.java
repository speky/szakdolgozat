package http.filehandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReportHandlerTest {

	private static Logger logger;

	@BeforeClass
	public static void prepare() {
		logger = new Logger("");		
	}

	@AfterClass
	public static void tearDown() {		
		logger.deleteLogFile();
	}

	class Wrapper implements ICallback {

		public int bytes = 0;		
		
		@Override
		public void receiveReportMessages(int receivedBytes) {
			bytes += receivedBytes;	
		}
		
	}
/*
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

		Vector<Integer> set = new Vector<Integer> ();
		ReportHandler handler = new ReportHandler(logger);
		handler.startReportReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(1000);
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

		Vector<Integer> set = new Vector<Integer> ();
		ReportHandler handler = new ReportHandler(logger);
		handler.startReportReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerWithWrongInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST / HTTP* /1.0\nREPO: 1");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		Vector<Integer> set = new Vector<Integer> ();
		ReportHandler handler = new ReportHandler(logger);
		handler.startReportReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue("Message sent successfully", set.size()==0);        
	}

	@Test
	public void testAckHandlerGoodInputWithoutEnd() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST / HTTP* /1.0\nREPORT: 1\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		Vector<Integer> set = new Vector<Integer> ();
		ReportHandler handler = new ReportHandler(logger);
		handler.startReportReceiver(null, "fileName", socket, set);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue(set.size()==0);        
	}

	@Test
	public void testAckHandlerGoodInput() {
		final Socket socket = mock(Socket.class);
		String str = new String("POST /fileName HTTP* /1.0\nREPORT: 2\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		Vector<Integer> set = new Vector<Integer> ();
		ReportHandler handler = new ReportHandler(logger);
		Assert.assertTrue(handler.startReportReceiver(null, "fileName", socket, set) == true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();
		Assert.assertTrue(set.size() == 1);		
		Assert.assertTrue(set.contains(2)==true);
	}
*/
	@Test
	public void testAckSend() {		
		Wrapper wrapper = new Wrapper();

		final Socket socket = mock(Socket.class);
		String str = new String("POST / HTTP*/1.0\nREPORT: 1\nEND\n");
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes());
		try {
			when(socket.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {			
			e.printStackTrace();
		}

		Vector<Integer> set = new Vector<Integer> ();		
		ReportHandler handler = new ReportHandler (logger);           
		handler.startReportReceiver(wrapper, "id", socket, set);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handler.stopScaning();	 
		Assert.assertTrue(wrapper.bytes == 1);
	}

}
