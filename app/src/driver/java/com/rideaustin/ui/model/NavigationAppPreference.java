package com.rideaustin.ui.model;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import timber.log.Timber;

/**
 * Created by supreethks on 01/10/16.
 */

public class NavigationAppPreference {

    private String appName;
    private String packageName;
    private String launcherActivity;

    private NavigationAppPreference(Builder builder) {
        appName = builder.appName;
        packageName = builder.packageName;
        launcherActivity = builder.launcherActivity;
    }


    public static final class Builder {
        private String appName;
        private String packageName;
        private String launcherActivity;

        public Builder() {
        }

        public Builder(ActivityInfo activityInfo, PackageManager pm) {
            appName = activityInfo.applicationInfo.loadLabel(pm).toString();
            packageName = activityInfo.packageName;
            launcherActivity = activityInfo.name;
            Timber.d("::Builder:: package name: %s Activity: %s", packageName, launcherActivity);
        }

        public Builder withAppName(String val) {
            appName = val;
            return this;
        }

        public Builder withPackageName(String val) {
            packageName = val;
            return this;
        }

        public Builder withLauncherActivity(String val) {
            launcherActivity = val;
            return this;
        }

        public NavigationAppPreference build() {
            return new NavigationAppPreference(this);
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getLauncherActivity() {
        return launcherActivity;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return "NavigationAppPreference{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", launcherActivity='" + launcherActivity + '\'' +
                '}';
    }
}
