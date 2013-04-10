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
	}

	public void testAddSection() throws Exception {
		String[] from = new String[] {"name", "value"};
		int[] to = new int[] { R.id.column_name, R.id.column_value};
		SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.grid, from, to);

		listAdapter.addSection("test", adapter);
		assertTrue(listAdapter.getCount() == 1 );
	}
	
}
