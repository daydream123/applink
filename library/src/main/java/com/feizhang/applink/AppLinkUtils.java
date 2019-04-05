package com.feizhang.applink;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangfei on 2015/9/8.
 */
public class AppLinkUtils {
    private static String sAppLinkPackage;

    @DrawableRes
    static int sSmallIcon;
    private static String sScheme;
    private static String sPrefix = "";

    /**
     * init appLink.
     */
    public static void setup(String appLinkPackage, @DrawableRes int smallIcon, String scheme){
        sAppLinkPackage = appLinkPackage;
        sSmallIcon = smallIcon;
        sScheme = scheme;
    }

    /**
     * init appLink
     */
    public static void setup(String appLinkPackage, @DrawableRes int smallIcon, String scheme, String prefix){
        sAppLinkPackage = appLinkPackage;
        sSmallIcon = smallIcon;
        sScheme = scheme;
        sPrefix = prefix;
    }

    private static void checkArgs(){
        if (TextUtils.isEmpty(sAppLinkPackage)) {
            throw new IllegalArgumentException("no appLinkPackage found, please init in application");
        }

        if (TextUtils.isEmpty(sScheme)) {
            throw new IllegalArgumentException("no scheme found, please init in application");
        }

        if (sSmallIcon == 0){
            throw new IllegalArgumentException("no small icon for notification found, please init in application");
        }
    }

    static boolean isSameScheme(String scheme){
        checkArgs();

        return TextUtils.equals(sScheme, scheme);
    }

    /**
     * Parse appLink and redirect to its target page with parameters.
     *
     * @param context android context
     * @param appLink appLink
     */
    public static void redirect(@NonNull Context context, @NonNull String appLink) {
        redirect(context, appLink, false);
    }

    /**
     * Parse URL and redirect to its page with parameters.
     *
     * @param context   android context
     * @param linkUrl   app link
     * @param forceBack true means appLink is come from push
     */
    public static void redirect(@NonNull Context context, @NonNull String linkUrl, boolean forceBack) {
        AppLink pushMessage = parseAppLink(context, linkUrl);
        if (pushMessage != null) {
            // if "forceBack" is "1", when back key is pressed, it will redirect to parent page
            // otherwise just close current page.
            if (forceBack) {
                pushMessage.getParams().put(AppLink.EXTRA_FORCE_BACK, "1");
            }

            // add "appVersion" for compatibility
            pushMessage.getParams().put(AppLink.EXTRA_APP_VERSION, getVersionName(context));

            Intent intent = pushMessage.onStartActivity(context);
            if (intent == null) {
                return;
            }

            // check can be resolved or no
            PackageManager manager = context.getPackageManager();
            ResolveInfo resolveInfo = manager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                context.startActivity(intent);
            }
        }
    }

    @Nullable
    public static AppLink parseAppLink(@NonNull Context context, @NonNull String appLink) {
        try {
            AppLinkParams params = readParams(context, appLink);
            @SuppressWarnings("unchecked")
            Class<? extends AppLink> moduleClass = (Class<? extends AppLink>) Class.forName(params.getClassName());
            AppLink pushMessage = moduleClass.newInstance();
            pushMessage.setAppLink(appLink);
            pushMessage.setParams(params.getParams());
            pushMessage.setJsonStr(params.getJsonStr());
            return pushMessage;
        } catch (Exception e) {
            Log.e(AppLink.TAG, "parseAppLink() error: " + e.getMessage());
        }
        return null;
    }

    @NonNull
    private static AppLinkParams readParams(@NonNull Context context, @NonNull String appLink) throws Exception {
        checkArgs();

        Pattern p = Pattern.compile(sScheme + "://([^?]*)");
        Matcher m = p.matcher(appLink);

        // find full url path
        String fullPath;
        if (!m.find()) {
            throw new Exception("appLink format is not valid for " + appLink);
        }

        fullPath = m.group(1);

        // build full class name
        StringBuilder builder = new StringBuilder(sAppLinkPackage);
        if (!fullPath.contains("/")){
            builder.append(".").append(fullPath);
        } else {
            String[] paths = fullPath.split("/");
            for (String path : paths) {
                builder.append(".").append(path.trim());
            }
        }

        // ignore word in package
        String fullClassPath = builder.toString();
        if (!TextUtils.isEmpty(sPrefix)) {
            fullClassPath = fullClassPath.replace(sPrefix, "");
        }

        // find params
        boolean haveValues = appLink.indexOf("?") > 0;
        if (!haveValues) {
            return new AppLinkParams(fullClassPath);
        } else {
            Map<String, String> values = AppLinkUtils.readParams(appLink);
            for (String key : values.keySet()) {
                if (key.equals("json")) {
                    String value = values.get(key);
                    return new AppLinkParams(fullClassPath, value == null ? "" : value.trim());
                }
            }

            return new AppLinkParams(fullClassPath, values);
        }
    }

    public static void pushAppLink(@NonNull Context context,
                                   @NonNull String appLink,
                                   @NonNull String title,
                                   @NonNull String subTitle,
                                   @Nullable String picUrl) {
        Intent intent = new Intent(PushNotificationReceiver.buildPushAction(context));
        intent.putExtra(PushReceiver.EXTRA_APP_LINK, appLink);
        intent.putExtra(PushReceiver.EXTRA_TITLE, title);
        intent.putExtra(PushReceiver.EXTRA_SUB_TITLE, subTitle);
        intent.putExtra(PushReceiver.EXTRA_PIC_URL, picUrl);
        context.sendOrderedBroadcast(intent, PushNotificationReceiver.buildPermission(context));
    }

    public static void pushAppLink(@NonNull Context context, @NonNull String appLink) {
        Intent intent = new Intent(PushNotificationReceiver.buildPushAction(context));
        intent.putExtra(PushReceiver.EXTRA_APP_LINK, appLink);
        context.sendOrderedBroadcast(intent, PushNotificationReceiver.buildPermission(context));
    }

    public static void refreshAppLink(@NonNull Context context, @NonNull String appLink) {
        Intent intent = new Intent(PushNotificationReceiver.buildPushAction(context));
        intent.putExtra(PushReceiver.EXTRA_APP_LINK, appLink);
        intent.putExtra(PushReceiver.EXTRA_REFRESH_ONLY, true);
        context.sendOrderedBroadcast(intent, PushNotificationReceiver.buildPermission(context));
    }

    public static boolean isWebUrl(@NonNull String url){
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        url = url.toLowerCase();
        return url.toLowerCase(Locale.getDefault()).startsWith("http://") || url.startsWith("https://");
    }

    public static boolean isAppLink(@NonNull String url) {
        checkArgs();
        return !(TextUtils.isEmpty(url) || TextUtils.isEmpty(sScheme)) && url.startsWith(sScheme);
    }

    @NonNull
    public static Map<String, String> readParams(@NonNull String url) {
        Map<String, String> map = new HashMap<>();
        int index = url.indexOf("?");
        if (index == -1) {
            return map;
        }

        String[] sections = url.split("\\?");
        for (String section : sections) {
            if (!section.contains("&") && !section.contains("=")){
                continue;
            }

            if (section.contains("&")){
                String[] keyValues = section.split("&");
                for (String keyValue : keyValues) {
                    readKeyValues(map, keyValue);
                }
            } else {
                readKeyValues(map, section);
            }
        }

        return map;
    }

    private static void readKeyValues(@NonNull Map<String, String> map, @NonNull String keyValue){
        if (!keyValue.contains("=")) {
            return;
        }

        String[] params = keyValue.split("=");
        if (params.length == 2) {
            map.put(params[0].trim(), URLDecoder.decode(params[1].trim()));
        }
    }

    private static String getVersionName(@NonNull Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppLink.TAG, "getVersionName() error: " + e.getMessage());
            return "";
        }
    }
}
