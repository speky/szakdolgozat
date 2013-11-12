package com.drivetesting.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.drivetesting.DataStorage;
import com.drivetesting.DbData;

import android.content.Context;
import android.test.AndroidTestCase;

public class DataStorageTest extends AndroidTestCase {
	private Context context = null;
	private DataStorage db = null;

	protected void setUp() throws Exception {
		super.setUp();
		db = new DataStorage(getContext());
	}

	public void testStoreOneItem() throws Exception {
		Calendar c = Calendar.getInstance(); 
		Date date = c.getTime();
		db.insert(1, "testName", 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		assertEquals(1, db.queryAll().size());
	}

	public void testStoreTwoItem() throws Exception {
		Calendar c = Calendar.getInstance(); 
		Date date = c.getTime();
		db.insert(1, "testName", 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		db.insert(2, "testName", 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		assertEquals(2, db.queryAll().size()); 
	}
}
