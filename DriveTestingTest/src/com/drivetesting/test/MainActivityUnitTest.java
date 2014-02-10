package com.drivetesting.test;

import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.drivetesting.MainActivity;

public class MainActivityUnitTest extends ActivityUnitTestCase<MainActivity>{

	private MainActivity mainActivity;
	private List<HashMap<String, String>> phoneDataList; 
	private List<HashMap<String, String>> networkDataList;

	public MainActivityUnitTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Starts the MainActivity of the target application
		startActivity(new Intent(getInstrumentation().getTargetContext(), MainActivity.class), null, null);

		// Getting a reference to the MainActivity of the target application
		mainActivity = (MainActivity)getActivity();
		assertNotNull(mainActivity);
		phoneDataList = mainActivity.getPhoneDataList();
		networkDataList = mainActivity.getNetworkDataList(); 
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@SmallTest
	public void testPhoneDefaultValues() {
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
	}

	@SmallTest
	public void testSetSignalStrength() {
		mainActivity.updateSignalStrength("3.2");		
		HashMap<String, String> map = phoneDataList.get(9);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Signal strength"));		
		assertTrue(map.get("value").equals("3.2dBm"));
	}
	
	@SmallTest
	public void testSetCdmaEcIo() {
		mainActivity.updateCdmaEcio("3.2");		
		HashMap<String, String> map = phoneDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Cdma EcIo"));		
		assertTrue(map.get("value").equals("3.2"));
	}
	
	@SmallTest
	public void testSetEvdodBm() {
		mainActivity.updateEvdoDbm("3.2");		
		HashMap<String, String> map = phoneDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo dBm"));		
		assertTrue(map.get("value").equals("3.2"));
	}
	
	@SmallTest
	public void testSetEvdoEcio() {
		mainActivity.updateEvdoEcio("3.2");		
		HashMap<String, String> map = phoneDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo EcIo"));		
		assertTrue(map.get("value").equals("3.2"));
	}
	
	@SmallTest
	public void testSetEvdoSNR() {
		mainActivity.updateEvdoSnr("3.2");		
		HashMap<String, String> map = phoneDataList.get(13);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo SNR"));		
		assertTrue(map.get("value").equals("3.2"));
	}
	
	@SmallTest
	public void testSetBitErrorRate() {
		mainActivity.updateGsmBitErrorRate("3.2");		
		HashMap<String, String> map = phoneDataList.get(14);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Bit error rate"));		
		assertTrue(map.get("value").equals("3.2"));
	}
	
	@SmallTest
	public void testNetworkDefaultValues() {
		HashMap<String, String> map = networkDataList.get(0);
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
		assertTrue(map.get("value").equals("None"));

		map = networkDataList.get(3);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Call state"));
		Log.d("call state", "value: " + map.get("value"));
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

		map = networkDataList.get(7);
		assertTrue(map.size() == 2);		
		assertTrue(map.get("name").equals("Data direction"));
		assertTrue(map.get("value").equals("-"));

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

	@SmallTest
	public void testSetServiceState() {
		mainActivity.updateServiceState("In service");		
		networkDataList = mainActivity.getNetworkDataList();
		HashMap<String, String> map = networkDataList.get(4);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Service state"));
		assertTrue(map.get("value").equals("In service"));
	}
	
	@SmallTest
	public void testSetCallState() {
		mainActivity.updateCallState("Idle");		
		networkDataList = mainActivity.getNetworkDataList();
		HashMap<String, String> map = networkDataList.get(3);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Call state"));
		assertTrue(map.get("value").equals("Idle"));
	}
	
	@SmallTest
	public void testSetNetworkState() {
		mainActivity.updateNetworkState("Connected");
		phoneDataList = mainActivity.getPhoneDataList();
		HashMap<String, String> map = networkDataList.get(6);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network state"));
		assertTrue(map.get("value").equals("Connected"));
	}
	
	@SmallTest
	public void testSetDataConnectionState() {
		mainActivity.updateDataConnectionState("Connected");
		phoneDataList = mainActivity.getPhoneDataList();
		HashMap<String, String> map = networkDataList.get(8);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Data connection state"));
		assertTrue(map.get("value").equals("Connected"));
	}
	
	@SmallTest
	public void testSetDataDirection() {
		mainActivity.updateDataDirection("In");		
		HashMap<String, String> map = networkDataList.get(7);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Data direction"));
		assertTrue(map.get("value").equals("In"));
	}
	
	@SmallTest
	public void testSetNetworkType() {
		mainActivity.updateNetworkType("UMTS");		
		HashMap<String, String> map = networkDataList.get(5);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network type"));
		assertTrue(map.get("value").equals("UMTS"));
	}
	
	@SmallTest
	public void testSetCellLocation() {
		mainActivity.updateCellLocation("1", "2", "3", "4");
		phoneDataList = mainActivity.getPhoneDataList();
		HashMap<String, String> map = networkDataList.get(9);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MNC"));
		assertTrue(map.get("value").equals("1"));
	
		map = networkDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MCC"));
		assertTrue(map.get("value").equals("2"));
	
		map = networkDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("LAC"));
		assertTrue(map.get("value").equals("3"));
	
		map = networkDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("CID"));
		assertTrue(map.get("value").equals("4"));	
}
	
}
