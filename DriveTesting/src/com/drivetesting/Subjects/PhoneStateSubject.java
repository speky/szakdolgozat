package com.drivetesting.Subjects;

import com.drivetesting.Observers.PhoneStateObserver;

public interface PhoneStateSubject {
	public void registerPhoneStateObserver(PhoneStateObserver observer);
	public void removePhoneStateObserver(PhoneStateObserver observer);
	public void notifySignalStrengthChange(String value);
	public void notifyCdmaEcioChange(String value);
	public void notifyEvdoDbmChange(String value);
	public void notifyEvdoEcioChange(String value);
	public void notifyEvdoSnrChange(String value);
	public void notifyGsmBitErrorRateChange(String value); 
	public void notifyServiceStateChange(String value);
	public void notifyCallStateChange(String value);
	public void notifyNetworkStateChange(String value);
	public void notifyDataConnectionStateChange(String value);
	public void notifyDataDirectionChange(String value);
	public void notifyNetworkTypeChange(String value);
	public void notifyCellLocationChange(String mnc, String mcc, String lac, String cid);
}
