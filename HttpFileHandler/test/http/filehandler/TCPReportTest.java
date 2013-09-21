package http.filehandler;

import junit.framework.Assert;

import org.junit.Test;

public class TCPReportTest {

	@Test
	public void testEmptyReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(false, report.parseReport(""));
	}

	@Test
	public void testFalseReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(false, report.parseReport("12 2sec 2.2 KB 3.2 11.2 Kbits/sec"));
		Assert.assertEquals(false, report.parseReport("13 2sec 2.2 KB 3.2 Kbits/sec "));
		Assert.assertEquals(false, report.parseReport("13 2sec 2.2 KB 3.2 Kbits/sec  11"));
	}
	
	@Test
	public void testPositiveReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseReport("1 2sec 2.2 KB 3.2 Kbits/sec 11.2 Kbits/sec"));
		Assert.assertEquals(true, report.parseReport("15 		2sec 2.2 KB 		3.2 Kbits/sec 11.2 Kbits/sec"));
	}
	
}
