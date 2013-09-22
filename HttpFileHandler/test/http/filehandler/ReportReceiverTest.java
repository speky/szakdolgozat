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
		
		ReportReceiver receiver = new ReportReceiver(logger, socket);
		Assert.assertTrue(false == receiver.parseReport(str));

		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
	}


	@Test
	public void testSendReportMessageTCP() {
		final Socket socket = mock(Socket.class);																					//Id Interval Transfer DL speed UL speed
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: 1 2sec 32 KB 23.2 KBits/sec 11.4 Kbits/sec");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket);
		Assert.assertTrue(true == receiver.parseReport(str));

		Assert.assertTrue(1 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
		
		TCPReport report = receiver.getTcpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(report.interval.equals("2sec"));
		Assert.assertTrue(32 == report.transferedData);
		Assert.assertTrue(23.2 == report.dlSpeed);
		Assert.assertTrue(11.4 == report.ulSpeed);
	}

	@Test
	public void testSendReportMessageUDP() {
		final Socket socket = mock(Socket.class);																					//Id Interval Transfer DL speed UL speed Jitter Lost/Total
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: UDP+MESSAGE: 1 2sec 32 KB 23.2 KBits/sec 11.4 Kbits/sec 1.1 2 5");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket);
		Assert.assertTrue(true == receiver.parseReport(str));

		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(1 == receiver.getUdpReportList().size());
		
		UDPReport report = receiver.getUdpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(report.interval.equals("2sec"));
		Assert.assertTrue(32 == report.transferedData);
		Assert.assertTrue(23.2 == report.dlSpeed);
		Assert.assertTrue(11.4 == report.ulSpeed);
		Assert.assertTrue(1.1 == report.jitter);
		Assert.assertTrue(2 == report.lostDatagram);
		Assert.assertTrue(5 == report.sumDatagram);
	}
}
