package com.dynamicload.framework.util;

import com.dynamicload.framework.dynamicload.internal.DLPluginManager;
import com.dynamicload.framework.dynamicload.internal.DLPluginPackage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.text.TextUtils;
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
    public static Map<String, String> soPathMap = new HashMap<String, String>();

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        FrameworkUtil.context = context;
    }

    public static void prepare() {
        prepareSoMap();
        loadDexAndService();
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
            if (bundleList != null) {
                String[] soNameList = bundleList.split(",");
                //load metainfo ,执行对应构造函数
                String dataPath = context.getFilesDir().getParent();
                Log.d("Launcher", dataPath);
                String lib = dataPath + "/lib/lib";

                for (String soname : soNameList) {
                    soPathMap.put("lib" + soname, lib + soname + ".so");
                }

            }
        } catch (IOException e) {
            Log.e("Util", "loadBundle Exception", e);
        }
    }

    public static boolean delete(String absPath) {
        if (TextUtils.isEmpty(absPath)) {
            return false;
        }

        File file = new File(absPath);
        return delete(file);
    }

    public static boolean delete(File file) {
        if (!exists(file)) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }

        boolean result = true;
        File files[] = file.listFiles();
        for (int index = 0; index < files.length; index++) {
            result |= delete(files[index]);
        }
        result |= file.delete();

        return result;
    }

    public static boolean exists(String absPath) {
        if (TextUtils.isEmpty(absPath)) {
            return false;
        }
        File file = new File(absPath);
        return exists(file);
    }

    public static boolean exists(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    private static void loadDexAndService() {
        //transfer so to temp apk.
        String apkRootPath = Environment.getExternalStorageDirectory().getPath() + "/" + PROJECT_NAME + "/" + context.getPackageName();
        for (Map.Entry<String, String> entry : FrameworkUtil.soPathMap.entrySet()) {
            //转换so为apk,但是产生了额外的文件,后面进行迭代删除
            apkRootPath = apkRootPath + "/" + entry.getKey() + ".apk";
            FrameworkUtil.replaceSoToApk(entry.getValue(), apkRootPath);
//            PluginItem item = new PluginItem();
//            item.pluginPath = apkRootPath;
//            item.packageInfo = DLUtils.getPackageInfo(context, item.pluginPath);
            DLPluginPackage dlPluginPackage = DLPluginManager.getInstance(context).loadApk(apkRootPath);
            //load Bundle`s service
//            DexClassLoader classLoader = DLPluginManager.getInstance(this).createDexClassLoader(apkRootPath);
            DexClassLoader classLoader = dlPluginPackage.classLoader;
            try {
                //load class
                PackageInfo info = dlPluginPackage.packageInfo; //DLUtils.getPackageInfo(context, apkRootPath);
                Class localClass = classLoader.loadClass(info.packageName + ".MetaInfo");
                //construct instance
                Constructor localConstructor = localClass.getConstructor(new Class[]{});
                //register Bundle`s service.
                localConstructor.newInstance();
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
        }
        //delete Apk
        for (Map.Entry<String, String> entry : FrameworkUtil.soPathMap.entrySet()) {
            FrameworkUtil.delete(apkRootPath + "/" + entry.getValue() + ".apk");
        }
    }

    private static class PluginItem {
        public PackageInfo packageInfo;
        public String pluginPath;

        public PluginItem() {
        }
    }
}
