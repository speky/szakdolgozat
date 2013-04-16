package com.drivetesting.test;

import com.drivetesting.OSMActivity;
import com.drivetesting.R;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class OSMTest extends  ActivityInstrumentationTestCase2<OSMActivity> {

	private OSMActivity activity;

	public OSMTest() {
		super(OSMActivity.class);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();  
	}
	
	public void testPreconditions() {
		assertNotNull(activity);
	}
	
	public void testPreferenceIsSaved() throws Exception {
		Instrumentation instr = getInstrumentation();		
		ListView view = (ListView) activity.findViewById(R.id.listview);

	}

}
