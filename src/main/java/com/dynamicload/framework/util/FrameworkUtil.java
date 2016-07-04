package com.dynamicload.framework.util;

import com.dynamicload.framework.dynamicload.internal.DLPluginManager;
import com.dynamicload.framework.dynamicload.internal.DLPluginPackage;
import com.dynamicload.framework.framework.model.Bundle;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import dalvik.system.DexClassLoader;

/**
 * Created by android_mc on 16/5/5.
 */
public class FrameworkUtil {

    private static final String TAG = "FrameworkUtil";

    private static final String PROJECT_NAME = "vivavideo";
    //key: H5Core(Module Name)  value: data/data/xxxxx/lib/libh5core.so
    public static Map<String, Bundle> soPathMap = new HashMap<String, Bundle>();

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        FrameworkUtil.context = context;
    }

    public static void prepare() {
        prepareSoMap();
        //loadDex  without lazy

        for (Map.Entry<String, Bundle> entry : FrameworkUtil.soPathMap.entrySet()) {
            Bundle bundle = entry.getValue();
            if (!bundle.isLazy) {
                loadDexAndService(bundle.bundleName, bundle.soPath);
            }
        }
    }

    private static void replaceSoToApk(String soPath, String apkPath) {

        File s = new File(soPath);
        if (!s.exists()) {
            return;
        }
        File t = new File(apkPath);
        if (t.exists()) {
            FileUtil.delete(t);
            Log.d("FrameworkUtil", "delete apk path:" + apkPath);
        }

        FileUtil.create(apkPath);

        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fi != null) {
                    fi.close();
                }
                if (in != null) {
                    in.close();
                }
                if (fo != null) {
                    fo.close();
                }
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getApkPathByBundleName(String bundleName) {
        Context context = FrameworkUtil.getContext();
        bundleName = "lib" + bundleName;
        String apkRootPath = Environment.getExternalStorageDirectory().getPath() + "/" + PROJECT_NAME + "/" + context.getPackageName();
        return apkRootPath + "/" + bundleName + ".apk";
    }

    private static void prepareSoMap() {
        Properties pro = new Properties();
        try {
            InputStream is = context.getApplicationContext().getAssets().open("bundlelist.properties");
            pro.load(is);
            String bundleList = pro.getProperty("bundleName");

            //not lazy list
            if (bundleList != null && !bundleList.isEmpty()) {
                String[] bundleNameList = bundleList.split(",");
                for (String bundleName : bundleNameList) {
                    Bundle bundle = new Bundle.Builder().bundleName(bundleName).lazy(false).serviceName("")
                            .soPath(context.getFilesDir().getParent() + "/lib/lib" + bundleName + ".so").build();
                    soPathMap.put(bundleName, bundle);
                }
            }
            String lazyList = pro.getProperty("lazyBundle");
            //lazy bundle
            if (lazyList != null && !lazyList.isEmpty()) {
                String[] lazyArrays = lazyList.split(",");
                for (int index = 0; index < lazyArrays.length; index++) {
                    String[] lazyBundle = lazyArrays[index].split("\\.");
                    Bundle bundle = new Bundle.Builder().bundleName(lazyBundle[0]).lazy(true).serviceName(lazyBundle[1])
                            .soPath(context.getFilesDir().getParent() + "/lib/lib" + lazyBundle[0] + ".so").build();
                    soPathMap.put(lazyBundle[0], bundle);
                }
            }
        } catch (IOException e) {
            Log.e("Util", "loadBundle Exception", e);
        }
    }

    public static void loadDexAndService(String bundleName, String soPath) {
        //transfer so to temp apk.
        //转换so为apk,但是产生了额外的文件,后面进行迭代删除
        long timeStart = System.currentTimeMillis();
        Log.d(TAG, "loadDex start time:" + timeStart);
        String apkRootPath = getApkPathByBundleName(bundleName);
        FrameworkUtil.replaceSoToApk(soPath, apkRootPath);
        Log.d(TAG, "replaceSoToApk:" + (System.currentTimeMillis() - timeStart));
        DLPluginPackage dlPluginPackage = DLPluginManager.getInstance(context).loadApk(apkRootPath);
        Log.d(TAG, "loadApk:" + (System.currentTimeMillis() - timeStart));
        DexClassLoader classLoader = dlPluginPackage.classLoader;
        try {
            //load class
            PackageInfo info = dlPluginPackage.packageInfo; //DLUtils.getPackageInfo(context, apkRootPath);
            Class localClass = classLoader.loadClass(info.packageName + ".MetaInfo");
            //construct instance
            Constructor localConstructor = localClass.getConstructor(new Class[]{});
            //register Bundle`s service.
            localConstructor.newInstance();
            Log.d(TAG, "newInstance:" + (System.currentTimeMillis() - timeStart));
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException ", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException ", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException ", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        //delete Apk
//        FileUtil.delete(apkRootPath);
    }
}
