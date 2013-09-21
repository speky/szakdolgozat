package http.filehandler;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class UDPReportTest {

	@Test
	public void testEmptyReport() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(false, report.parseReport(""));
	}

	@Test
	public void testFalseReport() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(false, report.parseReport("12 2sec 2.2 KB 3.2 Kbits/sec 11.2 Kbits/sec 1.1 2 	3"));
		Assert.assertEquals(false, report.parseReport("12 2sec 2.2 KB 3.2 Kbits/sec 11.2 Kbits/sec  2 	3 (23%)"));
		Assert.assertEquals(false, report.parseReport("12 2sec 2.2 KB 3.2 Kbits/sec 11.2 Kbits/sec 1.1 	3 (23%)"));
	}
	
	@Test
	public void testPositiveReport() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 2sec 2.2 KB 3.2 Kbits/sec 11.2 Kbits/sec 1.1 2 3 (23%)"));
		Assert.assertEquals(true, report.parseReport("15 		2sec 2.2 KB 		3.2 Kbits/sec 11.2 Kbits/sec	1.1 2 	3	(23%)"));
	}

}
