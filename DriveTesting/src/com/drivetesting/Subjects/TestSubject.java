package com.drivetesting.subjects;

import com.drivetesting.observers.TestObserver;

public interface TestSubject {

	public void registerObserver(TestObserver testObserver);
	public void removeObserver(TestObserver testObserver);
	public void notifyObservers();
}

