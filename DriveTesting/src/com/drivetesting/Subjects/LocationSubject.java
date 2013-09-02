package com.drivetesting.Subjects;

import com.drivetesting.Observers.LocationObserver;

public interface LocationSubject {
	public void registerObserver(LocationObserver observer);
	public void removeObserver(LocationObserver observer);
	public void notifyLocationObservers();
}
