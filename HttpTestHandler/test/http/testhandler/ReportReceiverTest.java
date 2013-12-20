package http.testhandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import http.testhandler.Logger;
import http.testhandler.ReportI;
import http.testhandler.ReportReceiver;
import http.testhandler.TCPReport;
import http.testhandler.UDPReport;
import http.testhandler.ReportReceiver.DataType;
import http.testhandler.ReportReceiver.RateType;

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
		final Socket socket = mock(Socket.class);																					//Id Interval Transfered data
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: 1 200  3200 ");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("TCP", "1 200 sec 3200.0 B 128.0 bits 0.0 bits");
		
		Assert.assertTrue(1 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
		
		TCPReport report = receiver.getTcpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(200 == report.interval);
		Assert.assertTrue(3200 == report.transferedData);
		Assert.assertTrue(128.0 == report.dlSpeed);
		Assert.assertTrue(0.0 == report.ulSpeed);
	}

	@Test
	public void testSendReportMessageTCPWithKbAndKbits() {
		final ReportI reporter = mock(ReportI.class);
		final Socket socket = mock(Socket.class);																					//Id Interval Transfered data
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: 1 200  3200 ");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		receiver.setData(DataType.KB);
		receiver.setRate(RateType.KBITS);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("TCP", "1 200 sec 3.125 KB 0.128 kbits 0.0 kbits");
		
		Assert.assertTrue(1 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());
		
	}
	
	@Test
	public void testSendReportMessageTCPWithMbAndMbits() {
		final ReportI reporter = mock(ReportI.class);
		final Socket socket = mock(Socket.class);																					//Id Interval Transfered data
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: TCP+MESSAGE: 1 200  3200");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		receiver.setData(DataType.MB);
		receiver.setRate(RateType.MBITS);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("TCP", "1 200 sec 0.0030517578125 MB 1.28E-4 mbits 0.0 mbits");
		
		Assert.assertTrue(1 == receiver.getTcpReportList().size());
		Assert.assertTrue(0 == receiver.getUdpReportList().size());		
	}
	
	
	
	@Test
	public void testSendReportMessageUDP() {
		final Socket socket = mock(Socket.class);																					//Id Interval Transfer Jitter Lost outOfOrder Total
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: UDP+MESSAGE: 1 200 3200 1.1 2 1 5");
		final ReportI reporter = mock(ReportI.class);
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		Assert.assertTrue(true == receiver.parseReport(str));
		
		Mockito.verify(reporter).sendMessage("UDP", "1 200 sec 3200.0 B 128.0 bits 0.0 bits 1.1 2 1 5");
		
		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(1 == receiver.getUdpReportList().size());
		
		UDPReport report = receiver.getUdpReportList().get(0);
		Assert.assertTrue(1 == report.reporterId);
		Assert.assertTrue(200 == report.interval);
		Assert.assertTrue(3200 == report.transferedData);
		Assert.assertTrue(128.0 == report.dlSpeed);
		Assert.assertTrue(0.0 == report.ulSpeed);
		Assert.assertTrue(1.1 == report.jitter);
		Assert.assertTrue(2 == report.lostDatagram);
		Assert.assertTrue(1 == report.outOfOrdered);
		Assert.assertTrue(5 == report.sumDatagram);
	}
	
	@Test
	public void testSendReportMessageUDPWithKbAndKbits() {
		final ReportI reporter = mock(ReportI.class);
		final Socket socket = mock(Socket.class);																					//Id Interval Transfered data
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: UDP+MESSAGE: 1 200  3200 1.1 3 3 12");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		receiver.setData(DataType.KB);
		receiver.setRate(RateType.KBITS);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("UDP", "1 200 sec 3.125 KB 0.128 kbits 0.0 kbits 1.1 3 3 12");
		
		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(1 == receiver.getUdpReportList().size());		
	}
	
	@Test
	public void testSendReportMessageUDPWithMbAndMbits() {
		final ReportI reporter = mock(ReportI.class);
		final Socket socket = mock(Socket.class);																					//Id Interval Transfered data
		StringBuffer str = new StringBuffer("POST 1 HTTP*/1.0+REPORT: UDP+MESSAGE: 1 200  3200 1.1 3 3 12");
		
		ReportReceiver receiver = new ReportReceiver(logger, socket, reporter);
		receiver.setData(DataType.MB);
		receiver.setRate(RateType.MBITS);
		Assert.assertTrue(true == receiver.parseReport(str));
																							  
		Mockito.verify(reporter).sendMessage("UDP", "1 200 sec 0.0030517578125 MB 1.28E-4 mbits 0.0 mbits 1.1 3 3 12");
		
		Assert.assertTrue(0 == receiver.getTcpReportList().size());
		Assert.assertTrue(1 == receiver.getUdpReportList().size());		
	}
}

