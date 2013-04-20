package com.drivetesting.test;

import com.drivetesting.ExportActivity;
import com.drivetesting.MainActivity;
import com.drivetesting.R;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ExportActivityTest extends	ActivityInstrumentationTestCase2<ExportActivity> {

	private ExportActivity activity;
	private TextView textView; 
	private EditText text;
	private RadioGroup radioGroup;
	private RadioButton radioText;
	private RadioButton radioKml;
	private Button export;
	
	public ExportActivityTest() {
		super(ExportActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		textView = (TextView) activity.findViewById(R.id.textView1);
		text = (EditText) activity.findViewById(R.id.output_file_name);
		radioGroup = (RadioGroup) activity.findViewById(R.id.radioGroup);
		radioText = (RadioButton) activity.findViewById(R.id.export_text);
		radioKml = (RadioButton) activity.findViewById(R.id.export_kml);
		export = (Button) activity.findViewById(R.id.export);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPreconditions() {
		assertNotNull(activity);
		assertNotNull(textView);
		assertNotNull(radioGroup);
		assertNotNull(radioText);
		assertNotNull(radioKml);
		assertNotNull(export);
	}
	
	public void testDefaultText() throws Exception {
		Instrumentation instr = getInstrumentation();
		
		assertTrue(textView.getText().equals("Output file name"));
		String txt = text.getText().toString();
		assertTrue(txt.equals(""));
		assertTrue(text.isEnabled());
		assertTrue(radioText.isChecked());
		assertTrue(radioText.getText().toString().equals("Export to plain text"));
		assertFalse(radioKml.isChecked());
		assertTrue(radioKml.getText().toString().equals("Export to KML format"));
		
	}
	
	public void testExportButton() throws Exception {
		Instrumentation instr = getInstrumentation();
		
		assertTrue(export.getText().equals("Start Export"));
			
	}
}
