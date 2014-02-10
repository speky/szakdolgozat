package com.drivetesting.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.drivetesting.DataStorage;
import com.drivetesting.DbData;

public class DataStorageTest extends AndroidTestCase {	
	private DataStorage db = null;

	private final  String COLUMNS = "id,test_id,test_name,time,lat,lon,signal_strenght,signal_level,up_speed,down_speed,jitter,lost_packet,sum_packet,mcc,mnc,lac,cid,rate,network_type";
	
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
		assertEquals(1, size);
	}

	public void testStoreTwoItem() throws Exception {	
		db.insert(1, "testName", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		db.insert(2, "testName1", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");		
		int size = db.queryAll().size();		
		assertEquals(2, size); 
		
		List<String> test = db.queryTestNames();
		assertTrue(test.size() == 2);
		assertTrue(test.get(0).equals("testName"));
		assertTrue(test.get(1).equals("testName1"));
		
		DbData data = db.queryLastInsertedRow();
		assertTrue(data.testName.equals("testName1"));
	}
	
	public void testColumnName() throws Exception {
		String names = db.getColunNames();
		assertTrue(names.equals(COLUMNS));
	}

	public void testQuery() throws Exception {	
		db.insert(1, "testName1", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");
		db.insert(2, "testName2", 1.1, 2.2, 3.3, 3, 4.4, 5.5, 6.6, 0, 1, 2, 3, 4, 5, 1, "UMTS");		
		int size = db.queryAll().size();		
		assertEquals(2, size);
		List<DbData> datas = null;
		datas = db.querySpecifiedTest("0");
		assertTrue(datas.size() == 0);
		datas = db.querySpecifiedTest("2");
		assertTrue(datas.size() == 1);
		datas.get(0).testName.equals("testName2");
		
		datas = db.querySpecifiedTestByName("testName0");
		assertTrue(datas.size() == 0);
		datas = db.querySpecifiedTestByName("testName1");
		assertTrue(datas.size() == 1);
		datas.get(0).testName.equals("testName1");
	}
}
