package com.drivetesting;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drivetesting.observers.LocationObserver;
import com.drivetesting.observers.TestObserver;


public class OSMActivity extends Activity implements LocationObserver, TestObserver {

	private final String TAG = "LocationActivity";
	private final String LAT = "latitude";
	private final String LON = "longitude";
	private final String TESTID = "testId";
	private final String ZOOM = "zoom";
	private final String CENTER = "center";
	
	private RoadManager roadManager = new OSRMRoadManager();
	private MapView mapView;
	private int noOfPoints;
	private IMapController osmvController;
	
	private boolean isAddNodes = true;
	private RoadNode roadNode = null;
	private RoadNode firstNode = null;
	private Road road = null;
	private PathOverlay roadOverlay;
	private ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;	
	private TextView testIdText;
	private long testId = -1;
	
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
		 
		testIdText = (TextView)this.findViewById(R.id.testIdText);
		
		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Map");

		mapView = (MapView) findViewById(R.id.mapView);
		
		sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		
		roadNode = new RoadNode();
		noOfPoints = 0;
		setHardwareAccelerationOff();		
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		osmvController = mapView.getController();		
		mapView.setTileSource(TileSourceFactory.MAPNIK);		
		
		//Add Scale Bar
		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		mapView.getOverlays().add(scaleBarOverlay);

		/*DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		locationOverlay =  new CustomItemizedIconOverlay(this, null, defaultResourceProxyImpl);
		//locationOverlay =  new MyLocationOverlay(getApplicationContext(), mapView);
		mapView.getOverlays().add(locationOverlay);
*/
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mapView);
		mapView.getOverlays().add( roadNodeMarkers);

		if (savedInstanceState == null) {               
			//no location known, we put a hard-coded map position:
			osmvController.setZoom(15);
			osmvController.setCenter(new GeoPoint(47.497147, 19.070567));
			testId = ((DriveTestApp)getApplication()).getTestId();			
		} else {
			osmvController.setZoom(savedInstanceState.getInt("zoom_level"));
			osmvController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));			
			testId = savedInstanceState.getLong("test_id");
		}
		setTestString();
		
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
		updateLoc(loc);	
	}
	
	private void setTestString() {
		if (testId == 0) {
			testIdText.setText("Test id: Undefined");
		} else {
			testIdText.setText("Test id:" + Long.toString(testId));
			loadRoadForTestId();
		}
	}
	
	private void loadRoadForTestId() {		
		List<DbData> data = ((DriveTestApp)getApplication()).queryTestData(testId);
		List<RoadNode> nodes = new ArrayList<RoadNode>();
		if (data != null) {	
			roadNodeMarkers.removeAllItems();
			for (int i = 0; i < data.size(); ++i) {
				RoadNode node = new RoadNode();				
				node.mLocation = new GeoPoint(data.get(i).lat, data.get(i).lon);
				node.mInstructions = data.get(i).toInstrustionString(); 
				nodes.add(node);
				++noOfPoints;
				addRoadNode(node);
			}
		}
		isAddNodes = false;
		firstNode = nodes.get(0);
		firstNode.mDuration = -1;
		roadNode = nodes.get(nodes.size()-1);
		getRoadAsync(roadNode.mLocation);
		osmvController.setCenter(firstNode.mLocation);
		mapView.postInvalidate();
	}
	
	public void onTestClick(View view) {
		List<String> list = ((DriveTestApp)getApplication()).getTestIds();
		list.add("All");
		final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
		
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Test ID");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	// load road for the testId            	
                testId = Long.parseLong(items[item].toString());
                setTestString();                
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((DriveTestApp)getApplication()).registerReportObserver(this);
		((DriveTestApp)getApplication()).startGPSService();
		load();
	} 

	@Override
	protected void onPause() {
		super.onPause();
		((DriveTestApp)getApplication()).removeReportObserver(this);
		save();
	}

	private void load() {		
		osmvController.setZoom(sharedPreferences.getInt(ZOOM, 15));
		float lat = sharedPreferences.getFloat(LAT, 0.0f);
		float lon = sharedPreferences.getFloat(LON, 0.0f);
		osmvController.setCenter(new GeoPoint(lat, lon));
		testId = sharedPreferences.getLong(TESTID, 0);
		setTestString();
	}	
	
	private void save() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(TESTID, testId);		
		editor.putInt(ZOOM, mapView.getZoomLevel());
		GeoPoint c = (GeoPoint) mapView.getMapCenter();
		editor.putFloat(LAT, c.getLatitudeE6());
		editor.putFloat(LON, c.getLongitudeE6());
		
		editor.commit();
	}
	
	@Override
	public void update(Location location) {		
		updateLoc(location);
		Log.d(TAG, "location update");		
	}

	private void updateLoc(Location location){
		GeoPoint geoPoint = new GeoPoint(location);
		osmvController.setCenter(geoPoint);	
		//locationOverlay.setLocation(geoPoint);		
		mapView.postInvalidate();			
	}
		
	private void putRoadNodes(){
		if (null == road){ 
			return;		
		}
		
		if (firstNode.mDuration == -1) {
			firstNode.mDuration = 0;
			firstNode.mLocation = road.mNodes.get(0).mLocation;
			addRoadNode(firstNode);
		}
		
		GeoPoint location = getLastPoint();
		if (location != null) {
			roadNode.mLocation = location;
			addRoadNode(roadNode);
		}
	}

	private GeoPoint  getLastPoint() {
		if (null == road){ 
			return null;
		}
		int size = road.mNodes.size();
		if (size == 0) {
			return null;
		}		
		return road.mNodes.get(size-1).mLocation;		
	}

	private void addRoadNode(RoadNode node) {
		if (node != null) {
			Drawable marker = getResources().getDrawable(R.drawable.marker_node);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step " + noOfPoints, node.mInstructions, node.mLocation, this);		
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			roadNodeMarkers.addItem(nodeMarker);
		}
	}
	
	public void getRoadAsync(GeoPoint newPoint){
		++noOfPoints;
		if (road == null && firstNode == null) {
			firstNode = new RoadNode();
			firstNode.mInstructions = roadNode.mInstructions;
			firstNode.mLocation = newPoint;
			firstNode.mDuration = -1;
			return;
		}
		
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();		
		waypoints.add(firstNode.mLocation);		
		waypoints.add(newPoint); 
		
		new UpdateRoadTask().execute(waypoints);
		mapView.postInvalidate();
	}

	void drawRoadOnMap(){
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		if (roadOverlay != null) {
			mapOverlays.remove(roadOverlay);
		}
		if (road == null){
			return;
		}

		if (road.mStatus == Road.STATUS_DEFAULT) {
			Toast.makeText(mapView.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		}
		roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());
		roadOverlay.setColor(Color.GREEN);
		Overlay removedOverlay = mapOverlays.set(0, roadOverlay);
		//Set the road overlay at the "bottom", just above the MapEventsOverlay to avoid covering the other overlays 
		mapOverlays.add(1, removedOverlay);
		
		if (isAddNodes) {
			putRoadNodes();	
		} else {
			isAddNodes = true;
		}
		mapView.postInvalidate();
	}

	// Async task to get the road in a separate thread.
	class UpdateRoadTask extends AsyncTask<Object, Void, Road> {		
		protected Road doInBackground(Object... params) {	
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];		
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			road = result;			
			drawRoadOnMap();                    
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

	@Override
	public void update(int action, String reports) {

	}

}
