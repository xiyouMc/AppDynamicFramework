package com.quvideo.xiaoying.framework.service;

/**
 * Created by android_mc on 16/5/4.
 */
public class ServiceDescription {

    private String interfaceName = "";

    private String className = "";

    private String bundleName = "";

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
