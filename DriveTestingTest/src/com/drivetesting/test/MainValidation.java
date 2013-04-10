package com.drivetesting.test;

import java.util.HashMap;
import java.util.List;

import com.drivetesting.MainActivity;
import com.drivetesting.R;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class MainValidation extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mainActivity;

	public MainValidation() {
		super(MainActivity.class);

	}

	@Override  
	protected void setUp() throws Exception {  
		super.setUp();  
		mainActivity = getActivity();  
		List<HashMap<String, String>> phoneDataList = mainActivity.getPhoneData();
		HashMap<String, String> map = phoneDataList.get(0);
		assertTrue(map.size() == 2);
		assertTrue(map.get("name").equals("Phone Type"));
		assertTrue(map.get("value").equals("GSM"));		

	}

	public void testPreconditions() {
		assertNotNull(mainActivity);
	}
	
	public void testPreferenceIsSaved() throws Exception {
		Instrumentation instr = getInstrumentation();
		
		ListView view = (ListView) mainActivity.findViewById(R.id.listview);

	}
}
