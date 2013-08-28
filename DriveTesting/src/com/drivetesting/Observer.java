package com.drivetesting;

public interface Observer {
	public void update(int action, String str);
}

interface PhoneStateObserver {
	public void updateSignalStrength(String value);
	public void updateCdmaEcio(String value);
	public void updateEvdoDbm(String value);
	public void updateEvdoEcio(String value);
	public void updateEvdoSnr(String value);
	public void updateGsmBitErrorRate(String value); 
	public void updateServiceState(String value);
	public void updateCallState(String value);
	public void updateNetworkState(String value);
	public void updateDataConnectionState(String value);
	public void updateDataDirection(String value);
	public void updateNetworkType(String value);
	public void updateCellLocation(String mnc, String mcc, String lac, String cid);
}
