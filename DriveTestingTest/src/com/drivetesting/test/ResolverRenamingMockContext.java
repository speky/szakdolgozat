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

    /**
     * The renaming prefix.
     */
    private static final String PREFIX = "test.";


    /**
     * The resolver.
     */
    private static final MockContentResolver RESOLVER = new MockContentResolver();

    /**
     * Constructor.
     * @param context 
     */
    public ResolverRenamingMockContext(Context context) {
        super(RESOLVER, new DelegatedMockContext(context));
    }

    public MockContentResolver getResolver() {
        return RESOLVER;
    }

    public void addProvider(String name, ContentProvider provider) {
        RESOLVER.addProvider(name, provider);
    }

    /**
     * The DelegatedMockContext.
     *
     */
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