package com.drivetesting.subjects;

import com.drivetesting.observers.LocationObserver;

public interface LocationSubject {
	public void registerObserver(LocationObserver observer);
	public void removeObserver(LocationObserver observer);
	public void notifyLocationObservers();
}
