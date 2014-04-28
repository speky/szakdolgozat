package http.testhandler;

import http.testhandler.TCPReport;
import http.testhandler.ReportReceiver.DataType;
import http.testhandler.ReportReceiver.RateType;
import junit.framework.Assert;

import org.junit.Test;

public class TCPReportTest {

	@Test
	public void testEmptyReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(false, report.parseReport(""));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("0 0 sec 0.0 B 0.0 bits 0.0 bits"));
	}

	@Test
	public void testPositiveReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(false, report.parseFullReport("12 2 sec 2.2 B 3.2 11.2 bits 11.2 bits"));
		Assert.assertEquals(false, report.parseFullReport("12 2 sec 2.2 KB 3.2 11.2 Kbits 11.2 Kbits"));	
				
	}
	
	@Test
	public void testFalseReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(false, report.parseReport("13 2 B 2.2 3.2"));
		Assert.assertEquals(false, report.parseReport("13 2 sec 2.2 3.2"));
	}
	
	@Test
	public void testDefaultReport() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseFullReport("0 0 sec 0.0 B 0.0 bits 0.0 bits"));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("0 0 sec 0.0 B 0.0 bits 0.0 bits"));
	}
	
	@Test
	public void testPositiveReportBaseSettings() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0 0.0 0.0"));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 0.0 bits 0.0 bits"));
	}
	
	@Test
	public void testPositiveReportBaseSettingsInKB() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseReport("1 100 123.0 0.0  0.0 "));
		report.setData(DataType.KB);
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 KB 0.0 bits 0.0 bits"));
	}

	@Test
	public void testPositiveReportBaseSettingsInMB() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseReport("1 100 123.0  0.0  0.0 "));
		report.setData(DataType.MB);		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 MB 0.0 bits 0.0 bits"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSetting() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseFullReport("1 100 sec 123.0 B 0.0 bits 0.0 bits"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 bits 1.2 bits"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSettingInKbits() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseReport("1 100 123.0 0.0 0.0"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		report.setRate(RateType.KBITS);
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 kbits 1.2 kbits"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSettingInMbits() {
		TCPReport report = new TCPReport();
		Assert.assertEquals(true, report.parseFullReport("1 100 sec 123.0 B 0.0 bits 0.0 bits"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		report.setRate(RateType.MBITS);
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 mbits 1.2 mbits"));
	}
}
