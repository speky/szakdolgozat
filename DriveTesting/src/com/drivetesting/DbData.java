package com.drivetesting;

public class DbData {
	public long id;
	public long testId;
	public String testName;
	public String time;
	public double lat;
	public double lon;		
	public double signalStrength;
	public double up;
	public double down;
	public int mcc;
	public int mnc;
	public int lac;
	public int cid;
	
	public DbData() {
		id = -1;
		testId = -1;
		testName= "-";
		time= "-";
		lat= 0.0;
		lon= 0.0;		
		signalStrength = 0.0;
		up= 0.0;
		down= 0.0;
		mcc = -1;
		mnc = -1;
		lac = -1;
		cid = -1;
	}
}
