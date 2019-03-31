package com.feizhang.applink;

import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import java.util.Map;

/**
 * Created by zhangfei on 2015/9/8.
 *
 * It's designed to register in Activity, Fragment or CustomView
 * to receive push message and do work which should only be done in them,
 * like refresh page and so on.
 */
public abstract class PushContentReceiver extends PushReceiver {

    public static void register(@NonNull Context context, @NonNull PushContentReceiver receiver, boolean highPriority) {
        if (isMainProcess(context)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PushNotificationReceiver.buildPushAction(context));
            if (highPriority) {
                intentFilter.setPriority(100);
            }
            context.registerReceiver(receiver, intentFilter, PushNotificationReceiver.buildPermission(context), null);
        }
    }

    public static void unregister(@NonNull Context context, @NonNull PushContentReceiver receiver) {
        if (isMainProcess(context)) {
            context.unregisterReceiver(receiver);
        }
    }

    /**
     * Specify appLink to receive or to handle.
     */
    public abstract String[] getAppLinksToCompare();

    /**
     * Return true to indicate that the push message has been handled and will not deliver it to other receiver.
     */
    public abstract boolean onReceive(@NonNull Context context, @NonNull AppLink appLink);

    /**
     * since different order detail page or product page may have only one activity, so here need to provide its key and value,
     * and they'll be used to check if same order detail page or same product page
     *
     * @return target key and value
     */
    public Pair<String, String> getTargetKeyValue() {
        return null;
    }

    @Override
    public final void onAppLinkReceive(@NonNull Context context, @NonNull AppLink appLink) {
        String appLinkUrl = appLink.getAppLink();

        // check if target appLink can receive by current receiver
        boolean matched = false;
        String[] appLinks = getAppLinksToCompare();
        for (String item : appLinks) {
            // check if configured appLink was included in received appLink
            // configured appLinks aways have no params and received appLinks may have params
            if (appLinkUrl.startsWith(item)) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            return;
        }

        Map<String, String> params = appLink.getParams();
        Pair<String, String> targetKeyValue = getTargetKeyValue();
        if (targetKeyValue == null) {
            // abort broadcast if already handled
            boolean handled = onReceive(context, appLink);
            if (handled) {
                abortBroadcast();
            }
        } else {
            // check if empty
            if (TextUtils.isEmpty(targetKeyValue.first) || TextUtils.isEmpty(targetKeyValue.second)) {
                return;
            }

            // the target page is current page
            String param = params.get(targetKeyValue.first);
            if (TextUtils.equals(targetKeyValue.second, param)) {
                // abort if already handled
                boolean handled = onReceive(context, appLink);
                if (handled) {
                    abortBroadcast();
                }
            }
        }
    }
}