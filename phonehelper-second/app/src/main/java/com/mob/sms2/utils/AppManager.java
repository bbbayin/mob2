package com.mob.sms2.utils;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AppManager {

    private static List<WeakReference<Activity>> appList = new ArrayList<>();

    public static void add(Activity activity) {
        appList.add(new WeakReference<>(activity));
    }

    public static WeakReference<Activity> getCurrentActivity() {
        if (appList != null && !appList.isEmpty()) {
            return appList.get(appList.size() - 1);
        }
        return null;
    }

    public static void pop() {
        if (appList != null && !appList.isEmpty()) {
            appList.remove(appList.size() - 1);
        }
    }
}
