package com.drivetesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.drivetesting.observers.LocationObserver;


public class OSMActivity extends Activity implements LocationObserver {

	private static final String TAG = "LocationActivity";
	private static final int OSRM=0, MAPQUEST_FASTEST=1, MAPQUEST_BICYCLE=2, MAPQUEST_PEDESTRIAN=3, GOOGLE_FASTEST=4;

	private MapView mapView;
	private IMapController osmvController;
	private CustomItemizedIconOverlay locationOverlay;
	private ArrayList<GeoPoint> viaPoints;
	private Road road;
	private PathOverlay roadOverlay;
	private ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;	
	private int whichRouteProvider;
	private Location currentLocation = null;

	private void setHardwareAccelerationOff(){
		// Turn off hardware acceleration here, or in manifest
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_osm);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Map");

		mapView = (MapView) findViewById(R.id.mapView);
		//locationText = (TextView)this.findViewById(R.id.locationInfo);

		setHardwareAccelerationOff();		
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		osmvController = mapView.getController();		
		mapView.setTileSource(TileSourceFactory.MAPNIK);		
		
		//Add Scale Bar
		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		mapView.getOverlays().add(scaleBarOverlay);
		
		viaPoints = new ArrayList<GeoPoint>();

		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mapView);
		mapView.getOverlays().add(roadNodeMarkers);

		if (savedInstanceState == null) {               
			//no location known, we put a hard-coded map position:
			osmvController.setZoom(15);
			osmvController.setCenter(new GeoPoint(48.55, 2.55));
			whichRouteProvider = OSRM;
		} else {
			whichRouteProvider = savedInstanceState.getInt("route_provider");
			currentLocation = ((Location)savedInstanceState.getParcelable("location"));
			osmvController.setZoom(savedInstanceState.getInt("zoom_level"));
			osmvController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));
			road = savedInstanceState.getParcelable("road");
			updateUIWithRoad(road);
		}

		viaPoints.add(new GeoPoint(48.5, 2.5));
		viaPoints.add(new GeoPoint(48.55, 2.55));
		//viaPoints.add(new GeoPoint(48.5, 2.6));

		getRoadAsync();

		DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		locationOverlay = new CustomItemizedIconOverlay(this, null, defaultResourceProxyImpl);
		mapView.getOverlays().add(locationOverlay);

		Location loc = new Location("dummyprovider");
		loc.setLongitude(2.55);
		loc.setLatitude(48.55);
		updateLoc(loc);

		List<Location> data = ((DriveTestApp)getApplication()).querryLocationData();
		if (data != null) {
			
		}
	}
	
	// callback to store activity status before a restart (orientation change for instance)
	@Override 
	protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("location", currentLocation);
		outState.putParcelableArrayList("viapoints", viaPoints);
		outState.putParcelable("road", road);
		outState.putInt("zoom_level", mapView.getZoomLevel());
		GeoPoint c = (GeoPoint) mapView.getMapCenter();
		outState.putParcelable("map_center", c);            
		MapTileProviderBase tileProvider = mapView.getTileProvider();
		int tileProviderOrdinal = tileProvider.getTileSource().ordinal();
		outState.putInt("tile_provider", tileProviderOrdinal);
		outState.putInt("route_provider", whichRouteProvider);
	}

	@Override
	protected void onResume() {
		super.onResume();		
		((DriveTestApp)getApplication()).registerObserver(this);
		((DriveTestApp)getApplication()).startGPSService();
	} 

	@Override
	protected void onPause() {
		super.onPause();
		((DriveTestApp)getApplication()).removeObserver(this);
	}

	@Override
	public void update(Location location) {		
		//this.locationText.setText("Lat: " + Double.toString(lat) + " Lon: " +Double.toString(lon));
		updateLoc(location);
		Log.d(TAG, "location update");		
	}
	
	private void updateLoc(Location location){
		GeoPoint geoPoint = new GeoPoint(location);
		osmvController.setCenter(geoPoint);	
		locationOverlay.setLocation(geoPoint);		
		mapView.postInvalidate();
	}
	
	//------------ Route and Directions

	private void putRoadNodes(Road road){
		roadNodeMarkers.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();		
		for (int i = 0;  i< n;  ++i) {
			RoadNode node = road.mNodes.get(i);
			String instructions = (node.mInstructions==null ? "" : node.mInstructions);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step " + (i+1), instructions, node.mLocation, this);
			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);			
			roadNodeMarkers.addItem(nodeMarker);
		}
	}

	void updateUIWithRoad(final Road road){
		roadNodeMarkers.removeAllItems();
		//TextView textView = (TextView)findViewById(R.id.routeInfo);
		//textView.setText("");
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
		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
		//Set the road overlay at the "bottom", just above the MapEventsOverlay to avoid covering the other overlays 
		mapOverlays.add(removedOverlay);
		//putRoadNodes(road);
		mapView.invalidate();
		//Set route info in the text view:
		//textView.setText(road.getLengthDurationText(-1));
	}

	
	 // Async task to get the road in a separate thread. 

	class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			RoadManager roadManager = null;
			Locale locale = Locale.getDefault();
			switch (whichRouteProvider){
			case OSRM:
				roadManager = new OSRMRoadManager();
				break;
			case MAPQUEST_FASTEST:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				break;
			case MAPQUEST_BICYCLE:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=bicycle");
				break;
			case MAPQUEST_PEDESTRIAN:
				roadManager = new MapQuestRoadManager();
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=pedestrian");
				break;			
			default:
				return null;
			}
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			road = result;
			updateUIWithRoad(result);                    
		}
	}

	public void getRoadAsync(){
		road = null;		
		updateUIWithRoad(road);

		//Settings.ACTION_WIRELESS_SETTINGS
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();		
		//add intermediate via points:
		for (GeoPoint p:viaPoints){
			waypoints.add(p); 
		}		
		new UpdateRoadTask().execute(waypoints);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_map).setVisible(false);

		final SubMenu subMenu = menu.addSubMenu(0, 12345, Menu.NONE,
				"Choose Tile Source");
		{
			ResourceProxy pxy = new DefaultResourceProxyImpl(this);
			for (final ITileSource tileSource : TileSourceFactory.getTileSources()) {
				subMenu.add(0, 1000 + tileSource.ordinal(), Menu.NONE, tileSource.localizedName(pxy));
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id)
		{

		case 12345:
			mapView.invalidate();
			return true;

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
