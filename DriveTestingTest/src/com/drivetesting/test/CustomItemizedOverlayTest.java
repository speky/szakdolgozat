package com.drivetesting.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.drivetesting.CustomItemizedOverlay;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

public class CustomItemizedOverlayTest 	extends AndroidTestCase {
	private Context context = null;
	private CustomItemizedOverlay overlay = null;
	
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
