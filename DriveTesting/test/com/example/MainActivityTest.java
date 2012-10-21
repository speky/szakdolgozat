package com.example;

import android.app.Activity;

import com.drivetesting.MainActivity;
import com.drivetesting.R;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void shouldHaveHappySmiles() throws Exception {
        Activity activity = new MainActivity(); 
    	String hello = activity.getResources().getString(R.string.app_name);
        assertThat(hello, equalTo("DriveTesting"));
    }
}
