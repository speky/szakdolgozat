package com.drivetesting;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import com.drivetesting.Observers.LocationObserver;
import com.drivetesting.Subjects.LocationSubject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class OSMActivity extends Activity implements LocationObserver {

	private static final String TAG = "LocationActivity";

	private MapView mapView;
	private TextView locationText;
	private MapController mapController;
	private LocationManager locationManager = null;

	private MyItemizedIconOverlay myItemizedIconOverlay = null;
	private MyLocationOverlay myLocationOverlay = null;

	private ArrayList<OverlayItem> overlayItemArray;
	
	private double lat = 0.0;
	private double lon = 0.0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_osm);

		ActionBar actionBar = getActionBar();	
		actionBar.setDisplayShowHomeEnabled(false) ;
		actionBar.setTitle("Map");
		
		locationText = (TextView)this.findViewById(R.id.locationInfo);
		mapView = (MapView) findViewById(R.id.mapView);
		//mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(2);
		
		double mIncr = 100.0;
        GeoPoint gPt0 = new GeoPoint(0, 0);
        GeoPoint gPt1 = new GeoPoint(gPt0.getLatitudeE6()+ mIncr, gPt0.getLongitudeE6());
        GeoPoint gPt2 = new GeoPoint(gPt0.getLatitudeE6()+ mIncr, gPt0.getLongitudeE6() + mIncr);
        GeoPoint gPt3 = new GeoPoint(gPt0.getLatitudeE6(), gPt0.getLongitudeE6() + mIncr);
        mapController.setCenter(gPt0);
		
        PathOverlay myPath = new PathOverlay(Color.RED, this);
        myPath.addPoint(gPt0);
        myPath.addPoint(gPt1);
        myPath.addPoint(gPt2);
        myPath.addPoint(gPt3);
        myPath.addPoint(gPt0);
        mapView.getOverlays().add(myPath);

		Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		Drawable marker2=getResources().getDrawable(R.drawable.marker);
		markerWidth = marker.getIntrinsicWidth();
		markerHeight = marker.getIntrinsicHeight();
		marker2.setBounds(0, markerHeight, markerWidth, 0);

		ResourceProxy resourceProxy = new DefaultResourceProxyImpl(this);
		/*//--- Create Overlay
        overlayItemArray = new ArrayList<OverlayItem>();

        myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, resourceProxy, this);
        mapView.getOverlays().add(myItemizedIconOverlay);
		 */   
		//GeoPoint myPoint1 = new GeoPoint(0*1000000, 0*1000000);
		//myItemizedIconOverlay.addItem(myPoint1, "myPoint1", "myPoint1");

		//--- Create Another Overlay for multi marker
		overlayItemArray = new ArrayList<OverlayItem>();
		overlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
		overlayItemArray.add(new OverlayItem("US", "US", new GeoPoint(38.883333, -77.016667)));
		overlayItemArray.add(new OverlayItem("China", "China", new GeoPoint(39.916667, 116.383333)));
		overlayItemArray.add(new OverlayItem("United Kingdom", "United Kingdom", new GeoPoint(51.5, -0.116667)));
		overlayItemArray.add(new OverlayItem("Germany", "Germany", new GeoPoint(52.516667, 13.383333)));
		overlayItemArray.add(new OverlayItem("Korea", "Korea", new GeoPoint(38.316667, 127.233333)));
		overlayItemArray.add(new OverlayItem("India", "India", new GeoPoint(28.613333, 77.208333)));
		overlayItemArray.add(new OverlayItem("Russia", "Russia", new GeoPoint(55.75, 37.616667)));
		overlayItemArray.add(new OverlayItem("France", "France", new GeoPoint(48.856667, 2.350833)));
		overlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(45.4, -75.666667)));

		ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay	= new ItemizedIconOverlay<OverlayItem>(this, overlayItemArray, myOnItemGestureListener);
		//mapView.getOverlays().add(anotherItemizedIconOverlay);
		//---
		
		/*Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
        if (location != null) {
	          Log.d(TAG, location.toString());
	          this.onLocationChanged(location); 
        }*/

		//Add Scale Bar
		ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
		mapView.getOverlays().add(myScaleBarOverlay);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();
	}

		/*try {
		      List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10); //<10>
		      for (Address address : addresses) {
		        this.locationText.append("\n" + address.getAddressLine(0));
		      }*/

		/*int latitude = (int)(location.getLatitude() * 1000000);
		int longitude = (int)(location.getLongitude() * 1000000);

		GeoPoint point = new GeoPoint(latitude,longitude);
		//mapController.animateTo(point);
		mapController.setCenter(point);
		overlayItemArray.clear();

		OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", point);
		overlayItemArray.add(newMyLocationItem);

		mapView.invalidate();
		*/

	@Override
	protected void onResume() {
		super.onResume();
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,  this);
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
	} 

	@Override
	protected void onPause() {
		super.onPause();
		//locationManager.removeUpdates(this);
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
	}

	@Override
	public void update(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		this.locationText.setText("Location: " + Double.toString(lat) + " " + Double.toString(lon));
		Log.d(TAG, "location update");		
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
			return false;			
		}
	}
	
	OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {
		@Override
		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
			
			return false;
		}

		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {
			Toast.makeText(OSMActivity.this, 
					item.mDescription + "\n"
							+ item.mTitle + "\n"
							+ item.mGeoPoint.getLatitudeE6() + " : " + item.mGeoPoint.getLongitudeE6(), 
							Toast.LENGTH_LONG).show();
			return true;
		}

	};

	private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem>{

		private Context context = null;
		
		public MyItemizedIconOverlay(
				List<OverlayItem> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy,  Context context) {
			super(pList, pOnItemGestureListener, pResourceProxy);
			this.context = context;
		}

		@Override
		public void draw(Canvas canvas, MapView mapview, boolean arg2) {
			super.draw(canvas, mapview, arg2);

			if (!overlayItemArray.isEmpty()){
				//overlayItemArray have only ONE element only, so I hard code to get(0)
				GeoPoint in = overlayItemArray.get(0).getPoint();

				Point out = new Point();
				mapview.getProjection().toPixels(in, out);

				/*  AssetManager assetManager = context.getAssets();
			        InputStream inputStream;
			        Bitmap bitmap
					try {
						inputStream = assetManager.open("marker.png");
				         BitmapFactory.Options options = new BitmapFactory.Options();
				        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
			        	inputStream.close();
					}catch (Exception e){

						e.printStackTrace();
					}*/

				Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
				canvas.drawBitmap(bm, 
						out.x - bm.getWidth()/2, 	//shift the bitmap center
						out.y - bm.getHeight()/2, 	//shift the bitmap center
						null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
			//return super.onSingleTapUp(event, mapView);
			return true;
		}
		
	}

	
}
