package com.drivetesting.Subjects;

import com.drivetesting.Observers.TestObserver;

public interface TestSubject {

	public void registerObserver(TestObserver testObserver);
	public void removeObserver(TestObserver testObserver);
	public void notifyObservers();
}

