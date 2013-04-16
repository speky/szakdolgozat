package com.drivetesting.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.widget.SimpleAdapter;

import com.drivetesting.R;
import com.drivetesting.SeparatedListAdapter;

public class SeparatedListTest extends AndroidTestCase{

	private SeparatedListAdapter listAdapter = null;
	private Context context = null;
	private List<HashMap<String, String>> dataList  = new ArrayList<HashMap<String, String>>();

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
		listAdapter = new SeparatedListAdapter(context);		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPreconditions() {
		assertNotNull(listAdapter);
		assertTrue(listAdapter.getViewTypeCount() == 1);
		assertTrue(listAdapter.getItemViewType(0) == -1);
				
	}

	public void testAddSection() throws Exception {
		String[] from = new String[] {"name", "value"};
		int[] to = new int[] { R.id.column_name, R.id.column_value};
		SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);
		listAdapter.addSection("test", adapter);
		
		assertTrue(listAdapter.getCount() == 1 );
		assertTrue(listAdapter.getViewTypeCount() == 2 );

		adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);
		listAdapter.addSection("test1", adapter);
		
		assertTrue(listAdapter.getCount() == 2);
		assertTrue(listAdapter.getViewTypeCount() == 3);
	}

	public void testAdapterGetItemByPosition() throws Exception {
		String[] from = new String[] {"name", "value"};
		int[] to = new int[] { R.id.column_name, R.id.column_value};
		SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);
		listAdapter.addSection("test", adapter);
					
		assertTrue(listAdapter.getItem(0).equals("test"));
		assertTrue(listAdapter.getItem(1) == null);
		
		listAdapter.addSection("test1", adapter);
		
		assertTrue(listAdapter.getItem(0).equals("test"));
		assertTrue(listAdapter.getItem(1).equals("test1"));
	}

	public void testAdapterGetItemViewTypeByPosition() throws Exception {
		String[] from = new String[] {"name", "value"};
		int[] to = new int[] { R.id.column_name, R.id.column_value};
		SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);
		listAdapter.addSection("test", adapter);
		listAdapter.addSection("test1", adapter);
		
		assertTrue(listAdapter.getItemViewType(0) == 0);
		assertTrue(listAdapter.isEnabled(0) == false);
		assertTrue(listAdapter.isEnabled(1) == false);
		
	}
	
	public void testAdapterGetItemIdByPosition() throws Exception {
		String[] from = new String[] {"name", "value"};
		int[] to = new int[] { R.id.column_name, R.id.column_value};
		SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);
		listAdapter.addSection("test", adapter);
		listAdapter.addSection("test1", adapter);
		
		assertTrue(listAdapter.getItemId(0) == 0);
		assertTrue(listAdapter.getItemId(1) == 1);
		
	}
	
}
