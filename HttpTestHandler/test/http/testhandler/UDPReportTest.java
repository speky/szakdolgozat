package http.testhandler;

import http.testhandler.UDPReport;
import http.testhandler.ReportReceiver.DataType;
import http.testhandler.ReportReceiver.RateType;
import junit.framework.Assert;

import org.junit.Test;

public class UDPReportTest {

	@Test
	public void testEmptyReport() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(false, report.parseReport(""));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("0 0 sec 0.0 B 0.0 bits 0.0 bits 0.0 0 0 0"));
	}
	
	@Test
	public void testDefaultReport() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(false, report.parseReport("0 0 sec 0.0 B 0.0 kbits 0.0 kbits  0 	0 0"));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("0 0 sec 0.0 B 0.0 bits 0.0 bits 0.0 0 0 0"));
	}
	
	@Test
	public void testPositiveReportBaseSettings() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100 123.0 1.3 1 1 3"));
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 0.0 bits 0.0 bits 1.3 1 1 3"));
	}
	
	@Test
	public void testPositiveReportBaseSettingsInKB() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0  1.3 1 1 3"));
		report.setData(DataType.KB);
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 KB 0.0 bits 0.0 bits 1.3 1 1 3"));
	}

	@Test
	public void testPositiveReportBaseSettingsInMB() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0 1.3 1 1 3"));
		report.setData(DataType.MB);
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 MB 0.0 bits 0.0 bits 1.3 1 1 3"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSetting() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0 1.3 1 1 3"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 bits 1.2 bits 1.3 1 1 3"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSettingInKbits() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0 1.3 1 1 3"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		report.setRate(RateType.KBITS);
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 kbits 1.2 kbits 1.3 1 1 3"));
	}
	
	@Test
	public void testPositiveReportWithSpeedSettingInMbits() {
		UDPReport report = new UDPReport();
		Assert.assertEquals(true, report.parseReport("1 100  123.0  1.3 1 1 3"));
		report.setDLSpeed(10.2);
		report.setULSpeed(1.2);
		report.setRate(RateType.MBITS);
		String output = report.toString();
		Assert.assertTrue(output.equals("1 100 sec 123.0 B 10.2 mbits 1.2 mbits 1.3 1 1 3"));
	}
}
