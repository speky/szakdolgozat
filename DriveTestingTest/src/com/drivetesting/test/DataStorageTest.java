package com.drivetesting.test;

import android.test.AndroidTestCase;
import android.util.Log;

import com.drivetesting.DataStorage;

public class DataStorageTest extends AndroidTestCase {	
	private DataStorage db = null;

	protected void setUp() throws Exception {
		super.setUp();
		db = new DataStorage(getContext());
		assertNotNull(db);
		db.open();
		db.deleteAll();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		db.deleteAll();
		db.close();
	}

	public void testStoreOneItem() throws Exception {		
		db.insert(1, "testName", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");		
		int size = db.queryAll().size();
		Log.d("SAD", Integer.toString(size));
		assertEquals(1, size);
	}

	public void testStoreTwoItem() throws Exception {		
		db.insert(1, "testName", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		db.insert(2, "testName", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");		
		int size = db.queryAll().size();
		Log.d("SAD", Integer.toString(size));
		assertEquals(2, size); 
	}
}
