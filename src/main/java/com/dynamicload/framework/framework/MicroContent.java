package com.dynamicload.framework.framework;


import android.content.SharedPreferences;

/**
 * Created by android_mc on 16/5/6.
 */
public abstract interface MicroContent {
    public abstract void restoreState(SharedPreferences paramSharedPreferences);

    public abstract void saveState(SharedPreferences.Editor paramEditor);
}
