package http.filehandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ReportReceiverTest {
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
	public void testSendFalseReportMessage() {
		final Socket socket = mock(Socket.class);
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: asd");
		final ReportI reporter = mock(ReportI.class);
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		Assert.assertTrue(false == receiver.parseReport(str));

		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
	}


	@Test
	public void testSendReportMessageTCP() {
		final ReportI reporter = mock(ReportI.class);
		final Socket socket = mock(Socket.class);																					//Id Interval Transfer DL speed UL speed
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: 1 2 sec 32 KB 23.2 KBits/sec 11.4 Kbits/sec");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("TCP", "1 2 sec 32.0 KB 23.2 Kbits/sec 11.4 Kbits/sec");
		
		Assert.assertTrue(1 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
		
		TCPReport report = receiver.getTcpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(2 == report.interval);
		Assert.assertTrue(32 == report.transferedData);
		Assert.assertTrue(23.2 == report.dlSpeed);
		Assert.assertTrue(11.4 == report.ulSpeed);
	}

	@Test
	public void testSendReportMessageUDP() {
		final Socket socket = mock(Socket.class);																					//Id Interval Transfer DL speed UL speed Jitter Lost/Total
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: UDP+MESSAGE: 1 2 sec 32 KB 23.2 KBits/sec 11.4 Kbits/sec 1.1 2 5");
		final ReportI reporter = mock(ReportI.class);
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		Assert.assertTrue(true == receiver.parseReport(str));
		
		Mockito.verify(reporter).sendMessage("UDP", "1 2 sec 32.0 KB 23.2 Kbits/sec 11.4 Kbits/sec 1.1 2 5 (40.0)");
		
		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(1 == receiver.getUdpReportList().size());
		
		UDPReport report = receiver.getUdpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(2 == report.interval);
		Assert.assertTrue(32 == report.transferedData);
		Assert.assertTrue(23.2 == report.dlSpeed);
		Assert.assertTrue(11.4 == report.ulSpeed);
		Assert.assertTrue(1.1 == report.jitter);
		Assert.assertTrue(2 == report.lostDatagram);
		Assert.assertTrue(5 == report.sumDatagram);
	}
}
