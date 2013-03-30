package com.drivetesting.test;

import java.util.HashMap;
import java.util.List;

import com.drivetesting.MainActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MainValidation extends ActivityInstrumentationTestCase2<MainActivity> {
	  	   
	   public MainValidation(Class<MainActivity> activityClass) {
		super(activityClass);
	
	}

	@Override  
	   protected void setUp() throws Exception {  
	       super.setUp();  
	       MainActivity mainActivity = getActivity();  
	       List<HashMap<String, String>> phoneDataList = mainActivity.getPhoneData();
	       for (int i = 0; i < phoneDataList.size(); i++) {
	    	   HashMap<String, String> map = phoneDataList.get(i);
	    	   assertTrue(map.get("Telefon típusa").equals(""));	    		
	    	   
	       }
	   }

}
