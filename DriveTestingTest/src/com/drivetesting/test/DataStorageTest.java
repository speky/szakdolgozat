package com.drivetesting.test;

import com.drivetesting.DataStorage;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

public class DataStorageTest extends AndroidTestCase {
	private Context context = null;
	private DataStorage db = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		final String filenamePrefix = "test.";
		MockContentResolver resolver = new MockContentResolver();
		RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
				new MockContext(), // The context that most methods are delegated to
				getContext(), // The context that file methods are delegated to
				filenamePrefix);
		context = new IsolatedContext(resolver, targetContextWrapper);
		setContext(context);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPreconditions() {
		//assertTrue(listAdapter.getViewTypeCount() == 1);
	}

}
