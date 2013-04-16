package com.drivetesting.test;

import java.io.File;

import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;


public class ResolverRenamingMockContext extends IsolatedContext {
    private static final String PREFIX = "test.";

   private static final MockContentResolver RESOLVER = new MockContentResolver();

    public ResolverRenamingMockContext(Context context) {
        super(RESOLVER, new DelegatedMockContext(context));
    }

    public MockContentResolver getResolver() {
        return RESOLVER;
    }

    public void addProvider(String name, ContentProvider provider) {
        RESOLVER.addProvider(name, provider);
    }

    private static class DelegatedMockContext extends MockContext {

        private Context mDelegatedContext;

        public DelegatedMockContext(Context context) {
            mDelegatedContext = context;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return mDelegatedContext.getSharedPreferences(PREFIX + name, mode);
        }
    }

}