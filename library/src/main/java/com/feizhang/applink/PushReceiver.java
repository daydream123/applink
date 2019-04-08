package com.feizhang.applink;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * It's a base push receiver, it main work is to save appLink into db if need,
 * after that it delivers push message to sub-receiver.
 */
public abstract class PushReceiver extends BroadcastReceiver {
    public static final String EXTRA_APP_LINK = "appLink";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SUB_TITLE = "subTitle";
    public static final String EXTRA_PIC_URL = "picUrl";
    public static final String EXTRA_REFRESH_ONLY = "refreshOnly";

    public static String buildPushAction(@NonNull Context context) {
        return context.getPackageName() + ".appLink.intent.action.PUSH";
    }

    public static String buildPermission(@NonNull Context context) {
        return context.getPackageName() + ".appLink.PERMISSION";
    }

    public abstract void onAppLinkReceive(@NonNull Context context, @NonNull AppLink appLink);

    public abstract String getAccount(@NonNull Context context);

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.e(AppLink.TAG, "no action in intent");
            return;
        }

        // check received message should be handled by current receiver
        if (TextUtils.equals(buildPushAction(context), action)) {
            String appLink = intent.getStringExtra(EXTRA_APP_LINK);
            String title = intent.getStringExtra(EXTRA_TITLE);
            String subTitle = intent.getStringExtra(EXTRA_SUB_TITLE);
            String picUrl = intent.getStringExtra(EXTRA_PIC_URL);
            boolean refresh = intent.getBooleanExtra(EXTRA_REFRESH_ONLY, false);

            AppLink link = AppLinkUtils.parseAppLink(context, appLink);
            if (link == null) {
                Log.e(AppLink.TAG, appLink + " cannot be resolved");
                return;
            }

            // try save it into db
            link.setId((int) (System.currentTimeMillis() / 1000));
            link.setTitle(title);
            link.setSubTitle(subTitle);
            link.setPicUrl(picUrl);
            link.setAccount(getAccount(context));

            if (link.shouldSave() && !refresh) {
                PushMessageService.getInstance(context).save(link);
            }

            // deliver message to sub-receiver
            onAppLinkReceive(context, link);
        }
    }

    protected static boolean isMainProcess(@NonNull Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }

        return false;
    }
}
