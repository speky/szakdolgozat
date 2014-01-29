package com.drivetesting;

import http.testhandler.ReportReceiver.RateType;
import http.testhandler.TCPReport;


public class DbData {
	public long id;
	public long testId;
	public String testName;
	public String time;
	public double lat;
	public double lon;		
	public double signalStrength;
	public int signalLevel;
	public double up;
	public double down;
	public double jitter;
	public int lost;
	public int sum;
	public int mcc;
	public int mnc;
	public int lac;
	public int cid;
	public int rate;
	public String networkType;	

	private String rateString = "";

	public DbData() {
		id = -1;
		testId = -1;
		testName= "-";
		time= "-";
		lat= 0.0;
		lon= 0.0;		
		signalStrength = 0.0;
		signalLevel = 0;
		up= 0.0;
		down= 0.0;
		jitter = 0.0;
		lost = 0;
		sum = 0;
		mcc = -1;
		mnc = -1;
		lac = -1;
		cid = -1;
		rate = 0;
		networkType = "";
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
		exportText.append(signalLevel +",");
		exportText.append(up +",");
		exportText.append(down +",");
		exportText.append(jitter +",");
		exportText.append(lost +",");
		exportText.append(sum +",");
		exportText.append(mcc +",");
		exportText.append(mnc +",");
		exportText.append(lac +",");
		exportText.append(cid + ",");
		exportText.append(rate + ",");
		exportText.append(networkType);
		return exportText.toString();
	}

	private void setRateString() {
		switch (rate) {
		case 1:			
			rateString = TCPReport.RateString.get(RateType.BITS);
			break;
		case 2:			
			rateString = TCPReport.RateString.get(RateType.KBITS);			
			break;
		case 3:			
			rateString = TCPReport.RateString.get(RateType.MBITS);
			break;
		}
	}
	public String toDescriptionString() {
		setRateString();
		StringBuilder str = new StringBuilder();
		str.append("Time: "+ time.toString() +"\n");
		str.append("Network Type: "+ networkType+"\n");
		str.append("Signal Strength: " + signalStrength +"dBm\n");
		str.append("Signal Level: " + signalLevel +"\n");
		str.append("Upload: "+ Double.toString(up) + " "+rateString+"\n");
		str.append("Download: "+ Double.toString(down) +" "+rateString+"\n");
		str.append("MCC: "+ mcc +"\n");
		str.append("MCC: "+ mcc +"\n");
		str.append("LAC: " + lac +"\n");
		str.append("CID: "+ cid + "\n");		
		return str.toString();
	}	
	
}
