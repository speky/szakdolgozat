package com.drivetesting.test;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.drivetesting.ExportActivity;
import com.drivetesting.MainActivity;
import com.drivetesting.OSMActivity;
import com.drivetesting.PrefsActivity;
import com.drivetesting.R;
import com.drivetesting.TestActivity;

public class MainActivityInstrumentTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mainActivity;
	private List<HashMap<String, String>> phoneDataList; 
	private List<HashMap<String, String>> networkDataList;

	public MainActivityInstrumentTest() {
		super(MainActivity.class);
	}

	@Override  
	protected void setUp() throws Exception {  
		super.setUp();
		setActivityInitialTouchMode(false);
		mainActivity = getActivity();
		assertNotNull(mainActivity);		
		phoneDataList = mainActivity.getPhoneDataList();
		networkDataList = mainActivity.getNetworkDataList();		
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void test2StartValues() throws Exception {
		HashMap<String, String> map = phoneDataList.get(0);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Phone type"));
		assertTrue(map.get("value").equals("GSM"));

		map = phoneDataList.get(1);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Phone number"));
		assertTrue(map.get("value").equals("15555215554"));


		map = phoneDataList.get(2);
		assertTrue(map.size() == 2);	
		assertTrue(map.get("name").equals("IMEI/ESN"));		 
		assertTrue(map.get("value").equals("000000000000000"));

		map = phoneDataList.get(3);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Manufacturer"));
		assertTrue(map.get("value").equals("unknown"));

		map = phoneDataList.get(4);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Model"));
		Log.d("MODEL", map.get("value"));
		assertTrue(map.get("value").equals("sdk"));

		map = phoneDataList.get(5);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("SIM state"));
		assertTrue(map.get("value").equals("Ready"));

		map = phoneDataList.get(6);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("SIM SN"));
		assertTrue(map.get("value").equals("89014103211118510720"));

		map = phoneDataList.get(7);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Software version"));		
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(8);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("IMSI"));
		assertTrue(map.get("value").equals("310260000000000"));

		map = phoneDataList.get(9);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Signal strength"));		 
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Cdma EcIo"));
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo dBm"));
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo EcIo"));
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(13);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo SNR"));
		assertTrue(map.get("value").equals("-"));

		map = phoneDataList.get(14);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Bit error rate"));
		assertTrue(map.get("value").equals("-"));
	
		//testNetworkDefaultValues
		map = networkDataList.get(0);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Operator"));		
		assertTrue(map.get("value").equals("Android"));

		map = networkDataList.get(1);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Country"));
		assertTrue(map.get("value").equals("US"));

		map = networkDataList.get(2);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Roaming"));
		assertTrue(map.get("value").equals("Nincs"));

		map = networkDataList.get(3);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Call state"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(4);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Service state"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(5);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network type"));
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(6);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network state"));
		assertTrue(map.get("value").equals("-"));

		/*map = networkDataList.get(7);
		assertTrue(map.size() == 2);		
		assertTrue(map.get("name").equals("Data direction"));		
		assertTrue(map.get("value").equals("-"));*/

		map = networkDataList.get(8);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Data connection state"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(9);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MNC"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MCC"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("LAC"));		
		assertTrue(map.get("value").equals("-"));

		map = networkDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("CID"));		
		assertTrue(map.get("value").equals("-"));
	}
	
	public void test1MenuTets() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(TestActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(mainActivity, R.id.menu_test, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
	public void test3MenuExport() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(ExportActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(mainActivity, R.id.menu_export, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}

	public void test4MenuSetting() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(PrefsActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(mainActivity, R.id.menu_settings, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
	public void test5MenuMap() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(OSMActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(mainActivity, R.id.menu_map, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 5000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
}
