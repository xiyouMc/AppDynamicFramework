package com.dynamicload.framework.framework.api;

import com.dynamicload.framework.dynamicload.internal.DLIntent;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Created by android_mc on 16/7/2.
 */
public interface MicroApplicationContext {

    public <T> T findServiceByInterface(String interfaceName);

    public Class<?> getClass(String classPath);

    public Resources getResourcesByBundle(String bundleName);

    public AssetManager getAssetsByBundle(String bundleName);

    public void startActivity(String bundleName, DLIntent intent);

    public Class<?> getClass(String bundleName, String classname);

    public void startActivityForResult(String bundleName, DLIntent intent, int requestCode);

    public void startActivityForResult(String bundleName, Activity activity, DLIntent intent, int requestCode);

    public void startExtActivityForResult(DLIntent intent, int requestCode);

    public void startExtActivity(DLIntent intent);
}
