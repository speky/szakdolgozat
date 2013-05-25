package com.drivetesting;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log; 

public class GPSService extends Service implements LocationListener {

	private static final String  TAG = "GPSService:"; 
	// The minimum distance to change Updates in meters
	private static final long DEFAULT_MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long DEFAULT_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	// The minimum time accuracy
	private static final long  DEFAULT_ACCURACY_CHANGE_FOR_UPDATES = 35;
	public static final String CUSTOM_INTENT = "com.drivetest.intent.action.TEST";

	private final Context context;
	private LocationManager locationManager;
	// flag for GPS status
	boolean isGPSEnabled = false; 
	// flag for network status
	boolean isNetworkEnabled = false; 
	boolean canGetLocation = false;

	Location location = null;
	double latitude = 0.0;
	double longitude = 0.0; 

	private static long minTimeMillis = DEFAULT_MIN_TIME_BW_UPDATES ;//2000;
	private static long minDistanceMeters = DEFAULT_MIN_DISTANCE_CHANGE_FOR_UPDATES;
	private static float minAccuracyMeters = DEFAULT_ACCURACY_CHANGE_FOR_UPDATES;
	
	public GPSService(Context context) {
		this.context = context;
		getLocation();                
	}   

	@Override
	public void onCreate() {
		super.onCreate();
		((DriveTestApp)getApplication()).setGpsService(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		((DriveTestApp)getApplication()).setGpsService(false);
	}

	private void shutdownLoggerService() {
		locationManager.removeUpdates(this);
	}
	
	private void broadcast() {
		System.out.println("*****HIT OUTGOING****************");
		Intent i = new Intent();
		i.setAction(CUSTOM_INTENT);
		this.context.sendBroadcast(i);
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);     
			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				Log.d(TAG, "*** Location service not available!");
			} else {
				this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							minTimeMillis,
							minDistanceMeters, this);
					Log.d(TAG,  "Network");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								minTimeMillis,
								minDistanceMeters, this);
						Log.d(TAG, "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						}
					}
				}
				if (location != null || (location.hasAccuracy() &&location.getAccuracy() <= minAccuracyMeters)) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
			}     
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
		return location;
	}

	//Function to get latitude        
	public double getLatitude(){
		return latitude;
	}

	// Function to get longitude        
	public double getLongitude(){		
		return longitude;
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	//Stop using GPS listener
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSService.this);
		}       
	}

	// Function to show settings dialog       
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// Setting Icon to Dialog
		//alertDialog.setIcon(R.drawable.delete);

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
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
		String showStatus = null;
		if (status == LocationProvider.AVAILABLE)
			showStatus = "Available";
		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			showStatus = "Temporarily Unavailable";
		if (status == LocationProvider.OUT_OF_SERVICE)
			showStatus = "Out of Service";
		
		Log.d(TAG, "status:" +status );
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
