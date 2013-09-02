package com.drivetesting;

public class DbData {
	public long id;
	public long testId;
	public String testName;
	public String time;
	public String lat;
	public String lon;		
	public String signalStrength;
	public String up;
	public String down;
	public int mcc;
	public int mnc;
	public int lac;
	public int cid;
	
	public DbData() {
		id = -1;
		testId = -1;
		testName= "-";
		time= "-";
		lat= "-";
		lon= "-";		
		signalStrength= "-";
		up= "-";
		down= "-";
		mcc = -1;
		mnc = -1;
		lac = -1;
		cid = -1;
	}
}
