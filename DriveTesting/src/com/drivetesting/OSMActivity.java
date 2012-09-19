package com.drivetesting;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class OSMActivity extends Activity implements LocationListener {

	private static final String TAG = "LocationActivity";
	
	private MapView mapView;
	private TextView locationText;
	private MapController mapController;
	private LocationManager locationManager = null;
		 
	MyItemizedOverlay myItemizedOverlay = null;
	MyItemizedOverlay myItemizedOverlay2 = null;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm);
        
        locationText = (TextView)this.findViewById(R.id.locationInfo);
        mapView = (MapView) findViewById(R.id.mapView);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(13);
        Log.d(TAG, "** app started " );
/*
        GeoPoint gPt0 = new GeoPoint(51500000, -150000);
        GeoPoint gPt1 = new GeoPoint(gPt0.getLatitudeE6()+ mIncr, gPt0.getLongitudeE6());
        GeoPoint gPt2 = new GeoPoint(gPt0.getLatitudeE6()+ mIncr, gPt0.getLongitudeE6() + mIncr);
        GeoPoint gPt3 = new GeoPoint(gPt0.getLatitudeE6(), gPt0.getLongitudeE6() + mIncr);
        mapController.setCenter(gPt0);*/
        /*
        PathOverlay myPath = new PathOverlay(Color.RED, this);
        myPath.addPoint(gPt0);
        myPath.addPoint(gPt1);
        myPath.addPoint(gPt2);
        myPath.addPoint(gPt3);
        myPath.addPoint(gPt0);
        mapView.getOverlays().add(myPath);*/
        
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
               
        Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);
        
        Drawable marker2=getResources().getDrawable(R.drawable.marker);
        markerWidth = marker.getIntrinsicWidth();
        markerHeight = marker.getIntrinsicHeight();
        marker2.setBounds(0, markerHeight, markerWidth, 0);
        
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
         
        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);
        
        myItemizedOverlay2 = new MyItemizedOverlay(marker2, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay2);
         
        GeoPoint myPoint1 = new GeoPoint(0*1000000, 0*1000000);
        myItemizedOverlay.addItem(myPoint1, "myPoint1", "myPoint1");
        GeoPoint myPoint2 = new GeoPoint(50*1000000, 50*1000000);
        myItemizedOverlay.addItem(myPoint2, "myPoint2", "myPoint2");
                
                
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
        if (location != null) {
	          Log.d(TAG, location.toString());
	          this.onLocationChanged(location); 
        }

        //Add Scale Bar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);
                
   }
    
	  @Override
	  public void onLocationChanged(Location location) {   
		  	Log.d(TAG, "onLocationChanged with location " + location.toString());
		    String text = String.format("Lat:\t %f\nLong:\t %f\nAlt:\t %f\nBearing:\t %f", location.getLatitude(), 
		                  location.getLongitude(), location.getAltitude(), location.getBearing());
		    this.locationText.setText(text);
		    
		    /*try {
		      List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10); //<10>
		      for (Address address : addresses) {
		        this.locationText.append("\n" + address.getAddressLine(0));
		      }*/
		      
		      int latitude = (int)(location.getLatitude() * 1000000);
		      int longitude = (int)(location.getLongitude() * 1000000);

		      GeoPoint point = new GeoPoint(latitude,longitude);
		      mapController.animateTo(point); 
		
	  }
	
	  @Override
	  public void onProviderDisabled(String provider) {
	   
	  }
	  @Override
	  public void onProviderEnabled(String provider) {
	   
	  }

	  @Override
	  public void onStatusChanged(String provider, int status, Bundle extras) {
   
	  }

    private void setOverlayLoc(Location overlayloc){
		 GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		 myItemizedOverlay.clear();
		 myItemizedOverlay.addItem(overlocGeoPoint, "loc", "loc");
     
    }
        
    @Override
    protected void onResume() {
    	  super.onResume();
    	  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,  this);
    } 
    
    @Override
    protected void onPause() {
		  super.onPause();
		  locationManager.removeUpdates(this); 
    }
}
