package com.drivetesting.test;

import com.drivetesting.ExportActivity;
import com.drivetesting.R;
import com.drivetesting.TestActivity;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class TestActivityTest extends	ActivityInstrumentationTestCase2<TestActivity> {

	private TestActivity activity;
	private TextView textView; 
	private RadioButton radioTcp;
	private RadioButton radioUdp;
	private TextView textView2; 
	private RadioButton radioDl;
	private RadioButton radioUl;
	private Button test;
	
	public TestActivityTest() {
		super(TestActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		textView = (TextView) activity.findViewById(R.id.textView2);
		radioTcp = (RadioButton) activity.findViewById(R.id.type_tcp);
		radioUdp = (RadioButton) activity.findViewById(R.id.type_udp);
		
		textView2 = (TextView) activity.findViewById(R.id.TextView01);
		radioDl = (RadioButton) activity.findViewById(R.id.dir_dl);
		radioUl = (RadioButton) activity.findViewById(R.id.dir_ul);
		
		test = (Button) activity.findViewById(R.id.bt_startTest);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testDefaultText() throws Exception {
		Instrumentation instr = getInstrumentation();
		
		assertTrue(textView.getText().equals("Protocol Type"));
		assertFalse(radioTcp.isChecked());
		assertTrue(radioTcp.getText().toString().equals("TCP"));
		assertTrue(radioUdp.isChecked());
		assertTrue(radioUdp.getText().toString().equals("UDP"));
		
		assertTrue(textView2.getText().equals("Direction"));
		assertTrue(radioDl.isChecked());
		assertTrue(radioDl.getText().toString().equals("Download"));
		assertFalse(radioUl.isChecked());
		assertTrue(radioUl.getText().toString().equals("Upload"));
				
	}
	
	public void testButton() throws Exception {
		Instrumentation instr = getInstrumentation();
		
		assertTrue(test.getText().equals("StartTest"));
			
	}

}
