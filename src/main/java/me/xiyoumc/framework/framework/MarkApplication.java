package me.xiyoumc.framework.framework;

import me.xiyoumc.framework.framework.api.MicroApplicationContext;

/**
 * Created by android_mc on 16/5/4.
 */
public class MarkApplication {

    private static MarkApplication mMarkApplication;

    private static MicroApplicationContext mMicroApplicationContext;

    private MarkApplication() {
    }

    public static MarkApplication getInstance() {
        if (mMarkApplication == null) {
            synchronized (MarkApplication.class) {
                if (mMarkApplication == null) {
                    mMarkApplication = new MarkApplication();
                }
            }
        }
        return mMarkApplication;
    }

    public synchronized MicroApplicationContext getMicroApplicationContext() {
        if (mMicroApplicationContext == null) {
            mMicroApplicationContext = new MicroApplicationContextImpl();
        }
        return mMicroApplicationContext;
    }
}
