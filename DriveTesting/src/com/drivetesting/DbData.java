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
	public double jitter;
	public int lost;
	public int sum;
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
		jitter = 0.0;
		lost = 0;
		sum = 0;
		mcc = -1;
		mnc = -1;
		lac = -1;
		cid = -1;
	}
	@Override
	public String toString() {
		StringBuilder exportText = new StringBuilder();
		exportText.append(id +",");
		exportText.append(testId +",");
		exportText.append(testName +",");
		exportText.append(time.toString() +",");
		exportText.append(lat +",");
		exportText.append(lon +",");
		exportText.append(signalStrength +",");
		exportText.append(up +",");
		exportText.append(down +",");
		exportText.append(jitter +",");
		exportText.append(lost +",");
		exportText.append(sum +",");
		exportText.append(mcc +",");
		exportText.append(mnc +",");
		exportText.append(lat +",");
		exportText.append(cid);
		return exportText.toString();
	}
}
