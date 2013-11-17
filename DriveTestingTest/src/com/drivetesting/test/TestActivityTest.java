package com.drivetesting.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.drivetesting.R;
import com.drivetesting.TestActivity;

public class TestActivityTest extends	ActivityInstrumentationTestCase2<TestActivity> {

	private TestActivity activity;
	private TextView textView; 
	private RadioButton radioTcp;
	private RadioButton radioUdp;
	private TextView textView2; 
	private RadioButton radioDl;
	private RadioButton radioUl;
	private Button startTest;
	private Button stopTest;
	private Button clear;
	private Button save;
	
	public TestActivityTest() {
		super(TestActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		assertNotNull(activity);
		textView = (TextView) activity.findViewById(R.id.textView2);
		assertNotNull(textView);
		radioTcp = (RadioButton) activity.findViewById(R.id.type_tcp);
		assertNotNull(radioTcp);
		radioUdp = (RadioButton) activity.findViewById(R.id.type_udp);
		assertNotNull(radioUdp);
		textView2 = (TextView) activity.findViewById(R.id.TextView01);
		assertNotNull(textView2);
		radioDl = (RadioButton) activity.findViewById(R.id.dir_dl);
		assertNotNull(radioDl);
		radioUl = (RadioButton) activity.findViewById(R.id.dir_ul);
		assertNotNull(radioUl);		
		startTest = (Button) activity.findViewById(R.id.bt_startTest);
		assertNotNull(startTest);
		stopTest = (Button) activity.findViewById(R.id.bt_stopTest);
		assertNotNull(stopTest);
		clear = (Button) activity.findViewById(R.id.buttonClear);
		assertNotNull(clear);
		save = (Button) activity.findViewById(R.id.buttonSave);
		assertNotNull(save);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testDefaultText() throws Exception {
				
		assertTrue(textView.getText().equals("Protocol Type"));
		assertTrue(radioTcp.isChecked());
		assertTrue(radioTcp.getText().toString().equals("TCP"));
		assertFalse(radioUdp.isChecked());
		assertTrue(radioUdp.getText().toString().equals("UDP"));
		
		assertTrue(textView2.getText().equals("Direction"));
		assertFalse(radioDl.isChecked());
		assertTrue(radioDl.getText().toString().equals("Download"));
		assertTrue(radioUl.isChecked());
		assertTrue(radioUl.getText().toString().equals("Upload"));
		
		assertTrue(startTest.getText().equals("Start test"));
		assertTrue(startTest.isEnabled());
		assertTrue(stopTest.getText().equals("Stop test"));
		assertFalse(stopTest.isEnabled());
		assertTrue(clear.getText().equals("Clear logs"));
		assertTrue(clear.isEnabled());
		assertTrue(save.getText().equals("Save logs"));
		assertTrue(save.isEnabled());
	}
	
	

}
