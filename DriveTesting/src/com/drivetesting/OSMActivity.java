package com.drivetesting;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.overlays. Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.drivetesting.observers.TestObserver;

public class OSMActivity extends Activity implements TestObserver {

	private final String LAT = "latitude";
	private final String LON = "longitude";
	private final String TESTID = "testId";
	private final String TESTNAME = "testName";
	private final String ZOOM = "zoom";

	private static int offset = 1;
	
	private RoadManager roadManager = new OSRMRoadManager();
	private MapView mapView;
	private int noOfPoints;
	private int nodeCount;
	private MapController controller;	
	private ScaleBarOverlay scaleBarOverlay = null;
	private List<RoadNode> nodes = null; 
	private RoadNode nodeA = null;
	private RoadNode nodeB = null;	
	private Road road = null;
	private List<DbData> dataList = null;
	private TextView testIdText;
	private long testId = -1;
	private String testName = "";

	private DriveTestApp application = null;
	private SharedPreferences sharedPreferences;

	private void setHardwareAccelerationOff(){
		// Turn off hardware acceleration here, or in manifest
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_osm);
		application = ((DriveTestApp)getApplication());
		testIdText = (TextView)this.findViewById(R.id.testIdText);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Map");

		mapView = (MapView) findViewById(R.id.mapView);

		sharedPreferences = getPreferences(Context.MODE_PRIVATE);

		noOfPoints = 0;
		nodeCount = 0;
		setHardwareAccelerationOff();		
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		controller = (MapController)mapView.getController();		
		mapView.setTileSource(TileSourceFactory.MAPNIK);		

		//Add Scale Bar
		scaleBarOverlay = new ScaleBarOverlay(this);

		/*DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		locationOverlay =  new CustomItemizedIconOverlay(this, null, defaultResourceProxyImpl);
		//locationOverlay =  new MyLocationOverlay(getApplicationContext(), mapView);
		mapView.getOverlays().add(locationOverlay);
		 */
		//final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		//roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mapView);
				
		controller.setZoom(15);		
		testId = application.getTestId();
		testName = application.getTestName();
		
		/* /blaha
		viaPoints.add(new GeoPoint(47.497147, 19.070567));

		viaPoints.add(new GeoPoint(47.497219, 19.069383));
		viaPoints.add(new GeoPoint(47.497994, 19.068972));

		viaPoints.add(new GeoPoint(47.493686, 19.069658));
		viaPoints.add(new GeoPoint(47.495769, 19.070244));

		 */
		Location loc = new Location("dummyprovider");
		loc.setLongitude(19.070567);
		loc.setLatitude(47.497147);
		// set camera to the location
		controller.setCenter(new GeoPoint(loc));						
		mapView.invalidate();	
	}

	@Override
	protected void onResume() {
		super.onResume();
		application.registerReportObserver(this);
		application.startGPSService();
		load();
	} 

	@Override
	protected void onPause() {
		super.onPause();
		application.removeReportObserver(this);
		save();
	}

	private void load() {		
		controller.setZoom(sharedPreferences.getInt(ZOOM, 15));
		GeoPoint p = new GeoPoint(47.497147, 19.070567);
		float lat = sharedPreferences.getFloat(LAT, (float) (p.getLatitudeE6()/1E6));
		float lon = sharedPreferences.getFloat(LON, (float) (p.getLongitudeE6()/1E6));
		controller.setCenter(new GeoPoint(lat, lon));
		testId = sharedPreferences.getLong(TESTID, 0);
		testName = sharedPreferences.getString(TESTNAME, "");
		// set active test id as active and show it on the map
		if (application.isTestRunning()) {
			testId = application.getTestId();			
		}

		if (testId == 0){
			setTestNameString();
		} else {
			setTestIdString();			
		}
		// set camera to the location
		controller.setCenter(new GeoPoint(lat, lon));						
		mapView.invalidate();
	}	

	private void save() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(TESTID, testId);
		editor.putString(TESTNAME, testName);
		editor.putInt(ZOOM, mapView.getZoomLevel());
		GeoPoint c = (GeoPoint) mapView.getMapCenter();		
		editor.putFloat(LAT, (float)(c.getLatitudeE6()/1E6));
		editor.putFloat(LON, (float)(c.getLongitudeE6()/1E6));

		editor.commit();
	}

	public void onTestClick(View view) {
		List<String> list = application.getTestIds();
		list.add("ALL");
		final CharSequence[] items = list.toArray(new CharSequence[list.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Test ID");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// load road for the testId
				try{
					testId = Long.parseLong(items[item].toString());
				}catch (NumberFormatException ex) {
					// ALL is selected
					testId = -1;
				}
				testName = "";
				setTestIdString();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void onTestNameClick(View view) {
		List<String> list = application.getTestNames();
		list.add("ALL");
		final CharSequence[] items = list.toArray(new CharSequence[list.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Test Name");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// load road for the testName
				testName = items[item].toString();
				testId = 0;
				setTestNameString();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setTestIdString() {
		if (testId == 0) {
			testIdText.setText("Test undefined");
		} else {
			if (-1 == testId) {
				testIdText.setText("Test id: ALL");
			} else {
				testIdText.setText("Test id:" + Long.toString(testId));
			}
			loadRoadForTestId();
		}
	}

	private void setTestNameString() {
		if (testName.equals("")) {
			testIdText.setText("Test: undefined");
		} else {
			testIdText.setText("Test name: " + testName);
			loadRoadForTestName();
		}
	}

	private void loadRoadForTestId() {		
		dataList = application.queryTestData(testId);
		nodeCount = dataList.size();
		clearMap();
		loadRoad();
	}

	private void loadRoadForTestName() {
		dataList = application.queryTestDataByName(testName);
		nodeCount = dataList.size();
		clearMap();
		loadRoad();
	}

	private void clearMap() {
		noOfPoints = 0;
		nodes = new ArrayList<RoadNode>();		
		mapView.getOverlays().clear();		
	}

	private RoadNode addRoadNode(DbData data) {
		RoadNode node = new RoadNode();
		 
		if (data.lat == 0.0) {
			data.lat = 10.0 + offset;
		}
		if (data.lon == 0.0) {
			data.lon = 10.0 +offset++;
		}
		
		node.mLocation = new GeoPoint(data.lat, data.lon);
		node.mInstructions = data.toDescriptionString();
		node.mDuration = data.signalLevel;
		nodes.add(node);		
		return node;
	}

	private void loadRoad() {		
		if (dataList == null || nodeCount <= 0) {
			return;
		}

		nodeA = nodeB;
		
		// handle the first node on the map
		if (0 == noOfPoints) {
			nodeA = addRoadNode(dataList.get(noOfPoints++));			
			--nodeCount;
			nodeB = nodeA;
		}
		
		// make all node item based on the query from the database 
		if (nodeCount > 0) {
			nodeB = addRoadNode(dataList.get(dataList.size() - nodeCount));
			++noOfPoints;
			ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();		
			waypoints.add(nodeA.mLocation);
			waypoints.add(nodeB.mLocation);			
			UpdateRoadTask task = new UpdateRoadTask();
			task.execute(waypoints);			
		} else {
			setOverlays();
		}

	}

	// Async task to get the road in a separate thread.
	class UpdateRoadTask extends AsyncTask<Object, Void, Road> {		

		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];		
			road = roadManager.getRoad(waypoints);
			return road;
		}

		@Override
		protected void onPostExecute(Road result) {
			drawRoadOnMap();
			--nodeCount;
			// set the camera at the last point and set the overlays on top of the roads 
			if (0 == nodeCount) {
				setOverlays();
			}else {
				loadRoad();
			}
		}		
	}

	private void setOverlays(){
		for (int i = 0; i < nodes.size(); ++i)		{
			addRoadMarker(nodes.get(i), i);
		}
		controller.setCenter(nodeB.mLocation);
		mapView.getOverlays().add(scaleBarOverlay);				
		mapView.invalidate();
	}

	private void drawRoadOnMap() {
		if (road == null){
			return;
		}
		/*if (road.mStatus == Road.STATUS_DEFAULT) {
			Toast.makeText(mapView.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		}*/

		Polyline roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());
		//set the road line color
		roadOverlay.setColor(Color.GREEN);

		mapView.getOverlays().add(roadOverlay);
		setProperLocationOnNodes();
		addRoadMarker(nodeB, noOfPoints);
	}

	private void setProperLocationOnNodes(){
		if (null == road || road.mNodes.size() <1){
			return;
		}
		nodeB.mLocation = road.mNodes.get(road.mNodes.size()-1).mLocation;
	}

	// add a  new road marker
	private void addRoadMarker(RoadNode node, int id) {
		if (node != null) {
			Drawable marker = null;

			switch ((int)node.mDuration) {
			case DriveTestApp.SIGNAL_UNKOWN :
				marker = getResources().getDrawable(R.drawable.marker_unknown);
				break;
			case DriveTestApp.SIGNAL_WEAK :
				marker = getResources().getDrawable(R.drawable.marker_weak);
				break;
			case DriveTestApp.SIGNAL_MODERATE :
				marker = getResources().getDrawable(R.drawable.marker_moderate);
				break;
			case DriveTestApp.SIGNAL_GOOD :
				marker = getResources().getDrawable(R.drawable.marker_good);
				break;
			case DriveTestApp.SIGNAL_GREAT :
				marker = getResources().getDrawable(R.drawable.marker_great);
				break;
			}
			
			Marker nodeMarker = new Marker(mapView);
			if (marker != null) {
				nodeMarker.setIcon(marker);
			}
			nodeMarker.setTitle("Id " + id);
			nodeMarker.setSnippet( node.mInstructions);
			nodeMarker.setPosition(node.mLocation);
			mapView.getOverlays().add(nodeMarker);			
		}
	}

	@Override
	public void update(int action, String reports) {
		if (DriveTestApp.ACTION_END == action ) {
			return;		
		}

		if (application.getTestId() == testId || application.getTestName().equals(testName)) {		
			DbData data = application.queryLastInsertedRow();
			if (null != data) {
				// add to the existing road
				dataList.clear();
				dataList.add(data);
				nodeCount = 1;		
				loadRoad();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_map).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id)
		{
		case R.id.menu_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;

		case R.id.menu_export:
			startActivity(new Intent(this, ExportActivity.class));
			return true;

		case R.id.menu_main:
			startActivity(new Intent(this, MainActivity.class));
			return true;

		case R.id.menu_test:
			startActivity(new Intent(this, TestActivity.class));
			return true;

		default:
			ITileSource tileSource = TileSourceFactory.getTileSource(item.getItemId() - 1000);
			mapView.setTileSource(tileSource);               
		}
		return false;
	}

}
