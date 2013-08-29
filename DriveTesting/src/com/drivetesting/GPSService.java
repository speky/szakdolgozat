package com.drivetesting;

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
import android.widget.Toast;

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

	private static long minTimeMillis = DEFAULT_MIN_TIME_BW_UPDATES ;//2000;
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
			locationManager.removeUpdates(GPSService.this);
		}       
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopUsingGPS();
		((DriveTestApp)getApplication()).setGpsService(false);
	}

	/*private void broadcast() {
		System.out.println("***** OUTGOING ******");
		Intent i = new Intent();
		i.setAction(CUSTOM_INTENT);
		getApplication().sendBroadcast(i);
	}*/

	public void getLocation() {
		try {
			// getting GPS status
			((DriveTestApp)getApplication()).isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);     
			// getting network status
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Location location = null;
			if (((DriveTestApp)getApplication()).isGPSEnabled == false &&  isNetworkEnabled == false) {
				// no network provider is enabled
				Log.d(TAG, "Location service not available!");				
			} else {				
				// if GPS Enabled get lat/long using GPS Services
				if (((DriveTestApp)getApplication()).isGPSEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							minTimeMillis,
							minDistanceMeters, this);
					Log.d(TAG, "use GPS provider");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}
				}

				// get location from Network Provider
				if (isNetworkEnabled && location == null) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							minTimeMillis,
							minDistanceMeters, this);
					Log.d(TAG,  "use Network provider");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

					}
				}				
				if (location != null /*&& (location.hasAccuracy() && location.getAccuracy() <= minAccuracyMeters)*/) {
					double latitude = location.getLatitude();
					double longitude = location.getLongitude();
					Log.d(TAG, "Update location: Lat: "+ Double.toString(latitude) + " Lon: "+ Double.toString(longitude));
					((DriveTestApp)getApplication()).updateLocation(location);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}		 
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			((DriveTestApp)getApplication()).updateLocation(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Provider disabled, "+ provider);
		((DriveTestApp)getApplication()).setGpsService(false);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Provider enabled, "+ provider);
		((DriveTestApp)getApplication()).setGpsService(true);
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

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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
}
