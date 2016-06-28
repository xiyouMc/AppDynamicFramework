package com.quvideo.xiaoying.framework.framework;

/**
 * Created by android_mc on 16/5/4.
 */
public class VivaApplication {

    private static VivaApplication mVivaApplication;

    private static MicroApplicationContext microApplicationContext;

    private VivaApplication() {
    }

    public static VivaApplication getInstance() {
        if (mVivaApplication == null) {
            synchronized (VivaApplication.class) {
                if (mVivaApplication == null) {
                    mVivaApplication = new VivaApplication();
                }
            }
        }
        return mVivaApplication;
    }

    public synchronized MicroApplicationContext getMicroApplicationContext() {
        if (microApplicationContext == null) {
            microApplicationContext = new MicroApplicationContext();
        }
        return microApplicationContext;
    }
}
