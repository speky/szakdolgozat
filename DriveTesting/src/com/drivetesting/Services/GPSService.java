package com.drivetesting.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.drivetesting.DriveTestApp;

public class GPSService extends Service implements LocationListener {

	private static final String  TAG = "GPSService:"; 
	// The minimum distance to change Updates in meters
	private static final long DEFAULT_MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long DEFAULT_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	// The minimum time accuracy
	private static final long  DEFAULT_ACCURACY_CHANGE_FOR_UPDATES = 35;
	public static final String CUSTOM_INTENT = "com.drivetest.intent.action.TEST";

	private LocationManager locationManager;

	private static long minTimeMillis = DEFAULT_MIN_TIME_BW_UPDATES;
	private static long minDistanceMeters = DEFAULT_MIN_DISTANCE_CHANGE_FOR_UPDATES;
	private static float minAccuracyMeters = DEFAULT_ACCURACY_CHANGE_FOR_UPDATES;

	@Override
	public void onCreate() {
		super.onCreate();
		((DriveTestApp)getApplication()).setGpsService(true);
		locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
	}	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		getLocation();
		return super.onStartCommand(intent, flags, startId);
	}

	//Stop using GPS listener
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}       
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopUsingGPS();
		((DriveTestApp)getApplication()).setGpsService(false);
	}

	private Location getGPSLocation() {
		//if GPS Enabled get lat/long using GPS Services
		if (((DriveTestApp)getApplication()).isGPSEnabled) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					minTimeMillis,
					minDistanceMeters, 
					this);
			Log.d(TAG, "use GPS provider");			
			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);						
		}
		return null;
	}

	private Location getNetworkProviderLocation(){
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				minTimeMillis,
				minDistanceMeters, 
				this);
		Log.d(TAG,  "use Network provider");
		return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	public void getLocation() {
		try {
			// getting GPS status
			((DriveTestApp)getApplication()).isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);     
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);				
			Location location = null;
			if (((DriveTestApp)getApplication()).isGPSEnabled == false &&  isNetworkEnabled == false) {
				// no network provider is enabled
				Log.d(TAG, "Location service not available!");				
			} else {
				location = getGPSLocation();					
				if (location == null && isNetworkEnabled){
					location = getNetworkProviderLocation();
				}
			}
			if (location != null /*&& (location.hasAccuracy() && location.getAccuracy() <= minAccuracyMeters)*/) {					
				Log.d(TAG, "Update location: Lat: "+ Double.toString(location.getLatitude()) + " Lon: "+ Double.toString(location.getLongitude()));
				((DriveTestApp)getApplication()).updateLocation(location);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}		 
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			((DriveTestApp)getApplication()).updateLocation(location);
			Log.d(TAG, "Update location: Lat: "+ Double.toString(location.getLatitude()) + " Lon: "+ Double.toString(location.getLongitude()));
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Provider disabled, "+ provider);	
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Provider enabled, "+ provider);		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		String showStatus = "";
		if (status == LocationProvider.AVAILABLE)
			showStatus = "Available";
		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			showStatus = "Temporarily Unavailable";
		if (status == LocationProvider.OUT_OF_SERVICE)
			showStatus = "Out of Service";

		Log.d(TAG, "status:" +showStatus );
	}

	public static void setMinTimeMillis(long _minTimeMillis) {
		minTimeMillis = _minTimeMillis;
	}

	public static long getMinTimeMillis() {
		return minTimeMillis;
	}

	public static void setMinDistanceMeters(long _minDistanceMeters) {
		minDistanceMeters = _minDistanceMeters;
	}

	public static long getMinDistanceMeters() {
		return minDistanceMeters;
	}

	public static float getMinAccuracyMeters() {
		return minAccuracyMeters;
	}

	public static void setMinAccuracyMeters(float _minAccuracyMeters) {
		minAccuracyMeters = _minAccuracyMeters;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		GPSService getService() {
			return GPSService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
