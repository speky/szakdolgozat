package com.drivetesting;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
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
		 
	MyItemizedIconOverlay myItemizedIconOverlay = null;
	
	ArrayList<OverlayItem> overlayItemArray;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm);
        
        locationText = (TextView)this.findViewById(R.id.locationInfo);
        mapView = (MapView) findViewById(R.id.mapView);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(12);
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
                             
        Drawable marker=getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);
        
        Drawable marker2=getResources().getDrawable(R.drawable.marker);
        markerWidth = marker.getIntrinsicWidth();
        markerHeight = marker.getIntrinsicHeight();
        marker2.setBounds(0, markerHeight, markerWidth, 0);
        
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(this);
        //--- Create Overlay
        overlayItemArray = new ArrayList<OverlayItem>();
        
        myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, resourceProxy, this);
        mapView.getOverlays().add(myItemizedIconOverlay);
         
        //GeoPoint myPoint1 = new GeoPoint(0*1000000, 0*1000000);
        //myItemizedIconOverlay.addItem(myPoint1, "myPoint1", "myPoint1");
        
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);        
        boolean enabledOnly = true;
        StringBuilder sb = new StringBuilder("Enabled Providers:");
        List<String> providers = locationManager.getProviders(enabledOnly);
        
        for (String provider : providers) {
        	locationManager.requestLocationUpdates(provider, 1000, 0, this);
	        sb.append("\n").append(provider).append(": ");
	        Location location = locationManager.getLastKnownLocation(provider);
	        if (location != null) {
		        double lat = location.getLatitude();
		        double lng = location.getLongitude();
		        sb.append(lat).append(", ").append(lng);
	        } else {
	        	sb.append("No Location");
	        }
	       
        }
        this.locationText.setText(sb);
        
        /*Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
        if (location != null) {
	          Log.d(TAG, location.toString());
	          this.onLocationChanged(location); 
        }*/

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
		      //mapController.animateTo(point);
		     mapController.setCenter(point);
		      overlayItemArray.clear();
		    	
		    	OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", point);
		    	overlayItemArray.add(newMyLocationItem);
		      
		      mapView.invalidate();
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
            
    @Override
    protected void onResume() {
    	  super.onResume();
    	  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,  this);
    	  //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	} 
    
    @Override
    protected void onPause() {
		  super.onPause();
		  locationManager.removeUpdates(this); 
    }
    
    private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem>{

    	private Context context;
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
