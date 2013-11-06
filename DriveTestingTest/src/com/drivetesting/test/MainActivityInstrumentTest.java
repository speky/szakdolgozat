package com.drivetesting.test;

import java.util.HashMap;
import java.util.List;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.ListView;

import com.drivetesting.MainActivity;
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

	/*public void testStartSecondActivity() throws Exception {

		// add monitor to check for the second activity
		ActivityMonitor monitor = getInstrumentation().addMonitor(TestActivity.class.getName(), null, false);

		// find button and click it
		Button view = (Button) activity.findViewById(R.id.button1);

		// TouchUtils handles the sync with the main thread internally
		TouchUtils.clickView(this, view);

		// to click on a click, e.g., in a listview
		// listView.getChildAt(0);

		// wait 2 seconds for the start of the activity
		TestActivity testActivity = (TestActivity) monitor.waitForActivityWithTimeout(2000);
		assertNotNull(testActivity);

		// search for the textView
		TextView textView = (TextView) testActivity.findViewById(R.id.resultText);

		// check that the TextView is on the screen
		ViewAsserts.assertOnScreen(startedActivity.getWindow().getDecorView(), textView);
		// validate the text on the TextView
		assertEquals("Text incorrect", "Started", textView.getText().toString());

		// press back and click again
		this.sendKeys(KeyEvent.KEYCODE_BACK);

		TouchUtils.clickView(this, view);
	}*/


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
		assertTrue(map.get("value").equals("-1"));

		map = phoneDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Cdma EcIo"));
		assertTrue(map.get("value").equals("-1"));

		map = phoneDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo dBm"));
		assertTrue(map.get("value").equals("-1"));

		map = phoneDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo EcIo"));
		assertTrue(map.get("value").equals("-1"));

		map = phoneDataList.get(13);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Evdo SNR"));
		assertTrue(map.get("value").equals("-1"));

		map = phoneDataList.get(14);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Bit error rate"));
		assertTrue(map.get("value").equals("-1"));
	}

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
		assertTrue(map.get("value").equals("Nincs"));

		map = networkDataList.get(3);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Call state"));		
		assertTrue(map.get("value").equals("Idle"));

		map = networkDataList.get(4);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Service state"));		
		assertTrue(map.get("value").equals("In service"));

		map = networkDataList.get(5);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network type"));
		assertTrue(map.get("value").equals("UMTS"));

		map = networkDataList.get(6);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Network state"));		
		assertTrue(map.get("value").equals("Connected"));

		map = networkDataList.get(7);
		assertTrue(map.size() == 2);		
		assertTrue(map.get("name").equals("Data direction"));		
		assertTrue(map.get("value").equals("InOut"));

		map = networkDataList.get(8);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Data connection state"));		
		assertTrue(map.get("value").equals("Connected"));

		map = networkDataList.get(9);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MNC"));		
		assertTrue(map.get("value").equals("0"));

		map = networkDataList.get(10);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("MCC"));
		assertTrue(map.get("value").equals("0"));

		map = networkDataList.get(11);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("LAC"));		
		assertTrue(map.get("value").equals("310"));

		map = networkDataList.get(12);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("CID"));		
		assertTrue(map.get("value").equals("260"));
	}

	public void testStateAfterRestart() throws Exception {		
		// Stop the activity - The onDestroy() method called
		mainActivity.finish();

		// Re-start the Activity - the onResume() method called
		mainActivity = getActivity();

		phoneDataList = mainActivity.getPhoneDataList(); 
		networkDataList = mainActivity.getNetworkDataList();


	}
}
