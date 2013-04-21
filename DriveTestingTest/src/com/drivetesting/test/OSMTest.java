package com.drivetesting.test;

import org.osmdroid.views.MapView;

import android.app.Instrumentation;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.drivetesting.OSMActivity;
import com.drivetesting.R;

public class OSMTest extends  ActivityInstrumentationTestCase2<OSMActivity> {

	private OSMActivity activity;
	private TextView locationText;
	private MapView map;
	private LocationManager locationManager = null;
	
	public OSMTest() {
		super(OSMActivity.class);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		locationText = (TextView) activity.findViewById(R.id.locationInfo);
		map = (MapView) activity.findViewById(R.id.mapView);
	}
	
	public void testPreconditions() {
		assertNotNull(activity);
		assertNotNull(locationText);
		assertNotNull(map);
		
	}
	
	public void testDefaultLayoutParameters() throws Exception {
		Instrumentation instr = getInstrumentation();		
		assertTrue(locationText.getText().toString().equals("Enabled Providers:\npassive: No Location\ngps: No Location"));

	}

}
