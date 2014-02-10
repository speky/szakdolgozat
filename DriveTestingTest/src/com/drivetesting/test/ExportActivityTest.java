package com.drivetesting.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.drivetesting.ExportActivity;
import com.drivetesting.MainActivity;
import com.drivetesting.OSMActivity;
import com.drivetesting.PrefsActivity;
import com.drivetesting.R;
import com.drivetesting.TestActivity;

public class ExportActivityTest extends	ActivityInstrumentationTestCase2<ExportActivity> {

	private ExportActivity activity;
	private TextView textView; 
	private EditText text;
	private EditText editTestId;
	private Button setTestId; 
	private Button export;
	
	public ExportActivityTest() {
		super(ExportActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		textView = (TextView) activity.findViewById(R.id.textView1);
		text = (EditText) activity.findViewById(R.id.output_file_name);		
		editTestId = (EditText) activity.findViewById(R.id.editTestId);
		setTestId = (Button) activity.findViewById(R.id.btn_settestid);
		export = (Button) activity.findViewById(R.id.export);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPreconditions() {
		assertNotNull(activity);
		assertNotNull(textView);
		assertNotNull(setTestId);
		assertNotNull(editTestId);
		assertNotNull(export);
	}
	
	public void testDefaultText() throws Exception {
		assertTrue(textView.getText().equals("Output file name"));
		String txt = text.getText().toString();
		assertTrue(txt.equals(""));
		assertTrue(text.isEnabled());
		
		String s = editTestId.getText().toString();
		assertTrue(s.equals("Test: undefined"));
		assertTrue(editTestId.isEnabled() == false);
		
		assertTrue(setTestId.getText().equals("Set Test ID"));
		assertTrue(setTestId.isEnabled());
				
		assertTrue(export.getText().equals("Export to CVS"));
		assertTrue(export.isEnabled() );		
	}
	
	public void testExport() throws Exception {
		 Instrumentation instrumentation = getInstrumentation();
	      	      				
		// Type the filename
	      View currentView = activity.findViewById(R.id.output_file_name);
	      assertNotNull(currentView);	      
	      TouchUtils.clickView(this, currentView);
	      instrumentation.sendStringSync("testFile");		
	      assertEquals(text.getText().toString(), "testFile");
	      
	   // Click the Id button...
	      currentView = activity.findViewById(R.id.btn_settestid);
	      assertNotNull(currentView);	      
	      TouchUtils.clickView(this, currentView);
	      
		
	}
	
	public void testMenuTets() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(TestActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(activity, R.id.menu_test, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
	public void testMenuMain() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(activity, R.id.menu_main, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}

	public void testMenuSetting() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(PrefsActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(activity, R.id.menu_settings, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 1000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
	public void testMenuMap() throws Exception {
		ActivityMonitor am = getInstrumentation().addMonitor(OSMActivity.class.getName(), null, false);
	
		// Click the menu option
		getInstrumentation().invokeMenuActionSync(activity, R.id.menu_map, 0);
	
		Activity a = getInstrumentation().waitForMonitorWithTimeout(am, 5000);
		assertEquals(true, getInstrumentation().checkMonitorHit(am, 1));
		a.finish();
	}
	
}
