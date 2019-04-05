package com.feizhang.applink;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.net.URL;

/**
 * It's designed to register in application to work as a global push message receiver.
 */
public abstract class PushNotificationReceiver extends PushReceiver {

    public static void register(@NonNull Context context, @NonNull PushNotificationReceiver receiver) {
        if (isMainProcess(context)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PushNotificationReceiver.buildPushAction(context));
            context.registerReceiver(receiver, intentFilter,
                    PushNotificationReceiver.buildPermission(context), null);
        }
    }

    /**
     * Override to provider small icon in push notification
     */
    public abstract int getSmallIcon(@NonNull Context context);

    @Override
    public void onAppLinkReceive(@NonNull Context context, @NonNull final AppLink appLink) {
        if (!appLink.showNotification(context)) {
            return;
        }

        // do any work but don't open activity
        appLink.onExecute(context);

        // build intent for starting activity
        Intent intent = appLink.onStartActivity(context);

        // not all appLinks have a target activity,
        // appLinks without target activity may do other works like send broadcast.
        if (intent == null) {
            return;
        }

        // no title cannot show notification
        if (TextUtils.isEmpty(appLink.getTitle())) {
            Log.d(AppLink.TAG, "no title cannot show notification for " + appLink.getAppLink());
            return;
        }

        // create notification builder
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = appLink.getBuilder(
                context,
                appLink.getTitle(),
                appLink.getSubTitle());
        builder.setSmallIcon(getSmallIcon(context));
        builder.setLargeIcon(getAppIcon(context));
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(Notification.DEFAULT_ALL);

        final NotificationManager manager = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = getNotificationChannel(context);
                manager.createNotificationChannel(channel);
                builder.setChannelId(channel.getId());
            }

            // notification with large picture
            if (!TextUtils.isEmpty(appLink.getPicUrl())) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(appLink.getPicUrl());
                            Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                            builder.setLargeIcon(bitmap);
                            builder.setStyle(new NotificationCompat
                                    .BigPictureStyle()
                                    .bigPicture(bitmap)
                                    .bigLargeIcon(null));
                            manager.notify(appLink.getId(), builder.build());
                        } catch (Exception ignored) {
                        }
                    }
                }.start();
            } else {
                manager.notify(appLink.getId(), builder.build());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNotificationChannel(@NonNull Context context) {
        NotificationChannel channel = new NotificationChannel(context.getPackageName(),
                getAppName(context), NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(View.VISIBLE);
        return channel;
    }

    @Nullable
    private static CharSequence getAppName(@NonNull Context context) {
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    @Nullable
    static Bitmap getAppIcon(@NonNull Context context) {
        PackageManager manager;
        ApplicationInfo info;

        try {
            manager = context.getApplicationContext().getPackageManager();
            info = manager.getApplicationInfo(context.getPackageName(), 0);
            Drawable drawable = manager.getApplicationIcon(info);
            return drawableToBitmap(drawable);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?
                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
