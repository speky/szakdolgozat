package com.drivetesting;

import http.filehandler.TCPReport;
import http.filehandler.ReportReceiver.DataType;
import http.filehandler.ReportReceiver.RateType;


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
	public int rate;
	public String networkType;	

	private String dataString;
	private String rateString;

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
			dataString = TCPReport.DataString.get(DataType.BYTE);
			rateString = TCPReport.RateString.get(RateType.BITS);
			break;
		case 2:
			dataString = TCPReport.DataString.get(DataType.KB);
			rateString = TCPReport.RateString.get(RateType.KBITS);			
			break;
		case 3:
			dataString = TCPReport.DataString.get(DataType.MB);
			rateString = TCPReport.RateString.get(RateType.MBITS);
			break;
		}
	}
	public String toInstrustionString() {
		setRateString();
		StringBuilder str = new StringBuilder();
		str.append("Time: "+ time.toString() +"\n");
		str.append("Network Type: "+ networkType+"\n");
		str.append("Signal Strength: " + signalStrength +"\n");
		str.append("Upload: "+ Double.toString(up) + "\n");
		str.append("Download: "+ Double.toString(down) + "\n");
		str.append("MCC: "+ mcc +"\n");
		str.append("MCC: "+ mcc +"\n");
		str.append("LAC: " + lac +"\n");
		str.append("CID: "+ cid + "\n");		
		return str.toString();
	}
}
