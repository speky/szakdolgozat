package com.drivetesting.subjects;

import com.drivetesting.observers.TestObserver;

public interface TestSubject {

	public void registerReportObserver(TestObserver testObserver);
	public void removeReportObserver(TestObserver testObserver);
	public void notifyReportObservers();
}

