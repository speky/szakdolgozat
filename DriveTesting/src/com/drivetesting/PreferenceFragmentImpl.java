package com.drivetesting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PreferenceFragmentImpl extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		
	}
}
