package com.mob.sms2.service;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.mob.sms2.BuildConfig;
import com.mob.sms2.bean.UpdateBean;
import com.mob.sms2.dialog.CheckTipDialog;
import com.mob.sms2.network.RetrofitHelper;
import com.mob.sms2.rx.BaseObserver;
import com.mob.sms2.rx.MobError;
import com.mob.sms2.utils.AppManager;
import com.mob.sms2.utils.ToastUtil;
import com.youth.banner.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;

public class UpdateService extends IntentService {

    private DownloadManager downloadManager;
    private File downloadApk;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateService(String name) {
        super(name);
    }

    public UpdateService() {
        this("update");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RetrofitHelper.getApi().checkUpdate()
                .subscribe(new BaseObserver<UpdateBean>() {
                    @Override
                    protected void onSuccess(UpdateBean data) {
                        if (data != null) {
                            String versioncode = data.getNo();
                            try {
                                int code = Integer.parseInt(data.getNo());
                                if (BuildConfig.VERSION_CODE < code) {
                                    // 升级
                                    WeakReference<Activity> currentActivity = AppManager.getCurrentActivity();
                                    if (currentActivity != null && currentActivity.get() != null) {
                                        CheckTipDialog dialog = new CheckTipDialog(currentActivity.get());

                                        dialog.setTitle("有新版本！");
                                        dialog.setContent(data.getRemark());
                                        dialog.setCancelListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        });
                                        dialog.setPositiveListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ToastUtil.show("开始下载，请关注通知栏的下载进度");
                                                downloadAndInstall(data.getUrl());
                                            }
                                        });
                                        dialog.show();
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("异常。。。");
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    protected void onFailed(MobError error) {

                    }
                });
    }

    private void downloadAndInstall(String url) {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("新版本下载中...");
            request.setTitle("隐藏拨号更新");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getApkName());
            long enqueue = downloadManager.enqueue(request);

            listen(enqueue);
        }

    }

    @NotNull
    private String getApkName() {
        return String.format("隐藏拨号_%s.apk", BuildConfig.VERSION_NAME);
    }

    private void listen(final long id) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (ID == id) {
                    // 检查权限
                    //兼容8.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // 判断是否有权限
                        boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
                        if(!haveInstallPermission){
                            Intent setting = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            setting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(setting);
                        }else {
                            Uri fileUri = downloadManager.getUriForDownloadedFile(id);
                            LogUtils.d("下载文件:"+fileUri.toString());
                            installAPK(fileUri);
                        }
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private Uri getUri(File file) {
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider2", file);
    }

    private void installAPK(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//      安装完成后，启动app（源码中少了这句话）
        if (null != uri) {
            try {
                //兼容7.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    //兼容8.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        boolean hasInstallPermission = getPackageManager().canRequestPackageInstalls();
                        if (!hasInstallPermission) {
                            startInstallPermissionSettingActivity();
                            return;
                        }
                    }
                } else {
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                    startActivity(intent);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
