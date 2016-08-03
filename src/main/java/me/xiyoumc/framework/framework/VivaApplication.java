package me.xiyoumc.framework.framework;

import me.xiyoumc.framework.framework.api.MicroApplicationContext;

/**
 * Created by android_mc on 16/5/4.
 */
public class VivaApplication {

    private static VivaApplication mVivaApplication;

    private static MicroApplicationContext mMicroApplicationContext;

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
        if (mMicroApplicationContext == null) {
            mMicroApplicationContext = new MicroApplicationContextImpl();
        }
        return mMicroApplicationContext;
    }
}
