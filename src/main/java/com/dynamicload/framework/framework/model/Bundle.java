package com.dynamicload.framework.framework.model;

/**
 * Created by android_mc on 16/7/4.
 */
public class Bundle {
    public String bundleName;
    public boolean isLazy;
    public String soPath;
    public String serviceName;

    public Bundle(Builder builder) {
        this.bundleName = builder.bundleName;
        this.isLazy = builder.isLazy;
        this.soPath = builder.soPath;
        this.serviceName = builder.serviceName;
    }

    public static class Builder {
        private String bundleName = "";
        private boolean isLazy = false;
        private String soPath = "";
        private String serviceName = "";

        public Builder bundleName(String bundleName) {
            this.bundleName = bundleName;
            return this;
        }

        public Builder lazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }

        public Builder soPath(String soName) {
            this.soPath = soName;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Bundle build() {
            return new Bundle(this);
        }
    }
}
