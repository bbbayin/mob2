package com.mob.sms2.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.mob.sms2.dialog.CheckTipDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Utils {
    public static boolean isNetworkAvailable(Context context) {

        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isAvailable();
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    public static String getYear() {
        long time = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(time));
        if (date.contains("-")) {
            return date.split("-")[0];
        } else {
            return "2021";
        }
    }

    public static long getTime(String str) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        try {
            Date date = df.parse(str);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getRandomNum(int number) {
        return new Random().nextInt(number);
    }

    public static void callPhone(String phoneNumber, Activity activity, int slotId) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            PhoneAccountHandle phoneAccountHandle = getPhoneAccountHandle(activity, slotId);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }

    /**
     * 拨打电话，根据sim卡
     *
     * @param activity
     * @param slotId   0:卡1  1:卡2
     */
    public static void callPhone(Activity activity, int slotId) {
        String phone = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        callPhone(phone, activity, slotId);
    }

    private static String TAG = "UTILS";

    private static PhoneAccountHandle getPhoneAccountHandle(Activity activity, int slotId) {
        TelecomManager tm = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<PhoneAccountHandle> handles = getAccountHandles(activity);
        if (handles != null && handles.size() > 0) {
            return handles.get(0);
        }
        SubscriptionManager sm = SubscriptionManager.from(activity);
        if (handles != null) {
            for (PhoneAccountHandle handle : handles) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                SubscriptionInfo info = sm.getActiveSubscriptionInfoForSimSlotIndex(slotId);
                if (info != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (TextUtils.equals(info.getIccId(), handle.getId())) {
                            Log.d(TAG, "getPhoneAccountHandle for slot" + slotId + " " + handle);
                            return handle;
                        }
                    } else {
                        if (TextUtils.equals(info.getSubscriptionId() + "", handle.getId())) {
                            Log.d(TAG, "getPhoneAccountHandle for slot" + slotId + " " + handle);
                            return handle;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<PhoneAccountHandle> getAccountHandles(Context context) {
        Class c;
        Method m;
        TelecomManager telecomManager;
        List<PhoneAccountHandle> accountHandles = new ArrayList<>();
        try {
            c = Class.forName("android.telecom.TelecomManager");
            Method m1 = c.getMethod("from", Context.class);
            telecomManager = (TelecomManager) m1.invoke(null, context);
            m = c.getMethod("getCallCapablePhoneAccounts");
            accountHandles = (List<PhoneAccountHandle>) m.invoke(telecomManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountHandles;
    }

    public static void showDialog(Activity activity, String content,
                                  String title,
                                  View.OnClickListener positiveListener, View.OnClickListener cancelListener) {
        CheckTipDialog tipDialog = new CheckTipDialog(activity);
        tipDialog.setTitle(title);
        tipDialog.setPositiveListener(positiveListener);
        tipDialog.setCancelListener(cancelListener);
        tipDialog.setContent(content);
        tipDialog.show();
    }

    public static String getCallInterval() {
        String interval = SPUtils.getString(SPConstant.SP_CALL_JGSZ, "");
        if (interval.contains("-")) {
            return String.format("随机%s秒", interval);
        } else {
            return String.format("%s秒", interval);
        }
    }

    public static int generateRandomInterval() {
        String interval = SPUtils.getString(SPConstant.SP_CALL_JGSZ, "");
        if (interval.contains("-")) {
            String[] split = interval.split("-");
            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);

            Random random = new Random();
            int rand = random.nextInt(max - min);
            return min + rand;
        } else {
            Random random = new Random();
            int rand = random.nextInt(15);
            return 15 + rand;
        }
    }

    /**
     * 跳转到MIUI应用权限设置页面
     *
     * @param context context
     */
    public static void jumpToPermissionsEditorActivity(Context context) {
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(mIntent);
    }

    /**
     * 判断是否是MIUI
     */
    private static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        if (device.equals("Xiaomi")) {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                return prop.getProperty("ro.miui.ui.version.code", null) != null
                        || prop.getProperty("ro.miui.ui.version.name", null) != null
                        || prop.getProperty("ro.miui.internal.storage", null) != null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
