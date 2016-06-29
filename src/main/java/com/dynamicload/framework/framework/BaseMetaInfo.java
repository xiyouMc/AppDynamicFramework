package com.dynamicload.framework.framework;


import com.quvideo.xiaoying.framework.service.ServiceDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android_mc on 16/5/4.
 */
public abstract class BaseMetaInfo {

    public static List<ServiceDescription> services;

    {
        services = new ArrayList<ServiceDescription>();
    }
}
