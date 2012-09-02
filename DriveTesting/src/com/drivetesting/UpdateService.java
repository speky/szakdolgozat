package com.drivetesting;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {

	static final String TAG = "UpdateService";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "StartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Create");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Destroy");
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
}
