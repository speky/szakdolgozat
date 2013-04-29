package com.drivetesting.test;

import java.util.Calendar;
import java.util.Date;

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
		/*MockContentResolver resolver = new MockContentResolver();
		RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
				new MockContext(), // The context that most methods are delegated to
				getContext(), // The context that file methods are delegated to
				filenamePrefix);
		context = new IsolatedContext(resolver, targetContextWrapper);
		setContext(context);*/

		db = new DataStorage(getContext());
	}

	public void testStoreOneItem() throws Exception {
		Calendar c = Calendar.getInstance(); 
		Date date = c.getTime();
		db.insert("lat", "ma", "10");
		//db.insert("lat long", date.toString(), "1");
		assertEquals(1, db.querry().size());
	}

	public void testStoreTwoItem() throws Exception {
		Calendar c = Calendar.getInstance(); 
		Date date = c.getTime();
		db.insert("lat long", date.toString(), "2");
		assertEquals(2, db.querry().size()); 
	}
}
