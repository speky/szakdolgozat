package com.drivetesting;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class UpdateService extends Service {

	static final String TAG = "UpdateService";

	private final IBinder mBinder = new MyBinder();
	private int num = -1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Random random = new Random();
		num =random.nextInt(20);
		
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		UpdateService getService() {
			return UpdateService.this;
		}
	}

	public int getNum() {
		return num;
	}

}