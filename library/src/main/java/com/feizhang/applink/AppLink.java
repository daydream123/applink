package com.feizhang.applink;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangfei on 2015/9/7.
 *
 * AppLink is a scheme string, a push message and also a db table record,
 * so we design appLink as a class with a lot of properties.
 */
public abstract class AppLink implements Serializable {
    static final String TAG = "appLink";

    public static final String EXTRA_FORCE_BACK = "forceBack";
    public static final String EXTRA_APP_VERSION = "appVersion";

    /**
     * table primary key
     */
    private int id;

    /**
     * used as notification's title
     */
    private String title;

    /**
     * used as notification's sub title
     */
    private String subTitle;

    /**
     * used as notification's image
     */
    private String picUrl;

    /**
     * app link url
     */
    private String appLink;

    /**
     * account id
     */
    private String account;

    /**
     * receive date in millis
     */
    private long receiveDt;

    /**
     * already read or not
     */
    private boolean read;

    protected Map<String, String> params = new HashMap<>();

    protected String jsonStr;

    /**
     * Override it to tell if the appLink need to save into db.
     */
    public boolean shouldSave() {
        return false;
    }

    /**
     * Build intent to start activity.
     */
    public Intent onStartActivity(@NonNull Context context) {
        return null;
    }

    /**
     * For same cases, they'll not start new activity when receive appLink,
     * you can do things like sending broadcast.
     */
    public void onExecute(@NonNull Context context){
    }

    /**
     * Override it to decide whether to show notification when appLink received from push service.
     */
    public boolean showNotification(Context context) {
        return true;
    }

    public void setParams(@NonNull Map<String, String> values) {
        this.params = values;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setJsonStr(@NonNull String jsonStr) {
        this.jsonStr = jsonStr;
    }

    /**
     * For personal appLink, account should not be empty.
     */
    public abstract boolean isPrivate();

    public NotificationCompat.Builder getBuilder(@NonNull Context context, @NonNull String title, @NonNull String subTitle) {
        return getDefaultBuilder(context, title, subTitle);
    }

    private NotificationCompat.Builder getDefaultBuilder(@NonNull Context context, @NonNull String title, @NonNull String subTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName());
        builder.setSmallIcon(AppLinkUtils.sSmallIcon);
        builder.setLargeIcon(PushNotificationReceiver.getAppIcon(context));
        builder.setContentTitle(title);
        if (!TextUtils.isEmpty(subTitle)) {
            builder.setContentText(subTitle);
        }
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        return builder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getAppLink() {
        return appLink;
    }

    public void setAppLink(String appLink) {
        this.appLink = appLink;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getReceiveDt() {
        if (receiveDt == 0) {
            receiveDt = System.currentTimeMillis();
        }
        return receiveDt;
    }

    public void setReceiveDt(long receiveDt) {
        this.receiveDt = receiveDt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
