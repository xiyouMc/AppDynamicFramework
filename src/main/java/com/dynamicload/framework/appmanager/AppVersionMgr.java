package com.dynamicload.framework.appmanager;

import com.quvideo.xiaoying.common.AppPreferencesSetting;
import com.quvideo.xiaoying.common.LogUtils;

import android.text.TextUtils;

import java.util.Locale;

public class AppVersionMgr {
    public static final String SDK_VERSION = "4.3.1";
    public static final String KEY_FIRST_LAUNCH_LANG_PRO = "key_first_launch_language_PRO";
    public static final int VERSION_AUTO = 1;
    public static final int VERSION_INTERNATIONAL = 2;
    private static final String TAG = "AppVersionMgr";
    private static final String KEY_FIRST_LAUNCH_LANG = "key_first_launch_language";
    public static String mStrLaunchLang = null;
    private static int mVersionFlag = VERSION_AUTO;

    public static void setVersionFlag(int nFlag) {
        mVersionFlag = nFlag;
    }

    public static boolean isVersionForInternational() {
        if (mVersionFlag == VERSION_INTERNATIONAL)
            return true;

        boolean isPro = AppPreferencesSetting.getInstance().getAppSettingBoolean(KEY_FIRST_LAUNCH_LANG_PRO, false);
        if (isPro) {
            return true;
        }

        //otherwise is VERSION_AUTO
        if (mStrLaunchLang == null) {
            String language = AppPreferencesSetting.getInstance().getAppSettingStr(KEY_FIRST_LAUNCH_LANG, "");
            if (TextUtils.isEmpty(language)) {
                language = Locale.getDefault().toString();
                AppPreferencesSetting.getInstance().setAppSettingStr(KEY_FIRST_LAUNCH_LANG, language);
            }
            LogUtils.i(TAG, "Preference langauge: " + language);
            mStrLaunchLang = language.toLowerCase(Locale.US);
        }

        if (mStrLaunchLang.equals("zh_cn")) {
            return false;
        } else {
            return true;
        }
    }
}
