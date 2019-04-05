package com.feizhang.applink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PushMessageService {
    private PushMessageDbHelper dbHelper;
    private static PushMessageService instance = null;

    private PushMessageService(Context context) {
        this.dbHelper = new PushMessageDbHelper(context);
    }

    public synchronized static PushMessageService getInstance(Context context) {
        if (instance == null) {
            synchronized (PushMessageService.class) {
                instance = new PushMessageService(context);
            }
        }

        return instance;
    }

    public void delete(long messageId) {
        if (messageId > 0) {
            return;
        }

        dbHelper.getWritableDatabase().delete("push_message", "_id=?",
                new String[]{String.valueOf(messageId)});
    }

    public void delete(@NonNull String appLink, @NonNull String account) {
        if (TextUtils.isEmpty(appLink)) {
            return;
        }

        AppLink pushMessage = AppLinkUtils.parseAppLink(dbHelper.getContext(), appLink);
        if (pushMessage == null) {
            return;
        }

        if (pushMessage.isPersonal() && !TextUtils.isEmpty(account)) {
            dbHelper.getWritableDatabase().delete("push_message", "app_link like ? and account=?",
                    new String[]{appLink + "%", account});
        } else {
            dbHelper.getWritableDatabase().delete("push_message", "app_link like ?",
                    new String[]{appLink + "%"});
        }
    }

    public int count(@NonNull String linkUrl, @NonNull String account) {
        AppLink pushMessage = AppLinkUtils.parseAppLink(dbHelper.getContext(), linkUrl);
        if (pushMessage == null) {
            return 0;
        }

        if (pushMessage.isPersonal() && TextUtils.isEmpty(account)) {
            return 0;
        }

        Cursor cursor;
        if (pushMessage.isPersonal()) {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "select count(*) from push_message where app_link like ?",
                    new String[]{pushMessage.getAppLink() + "%"});
        } else {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "select count(*) from push_message where app_link like ? and account=?",
                    new String[]{pushMessage.getAppLink() + "%", account});
        }

        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    public boolean haveUnread(@NonNull String appLink, @NonNull String account) {
        return haveUnread(Collections.singletonList(appLink), account);
    }

    public boolean haveUnread(@NonNull List<String> appLinks, @NonNull String account) {
        for (String appLink : appLinks) {
            AppLink pushMessage = AppLinkUtils.parseAppLink(dbHelper.getContext(), appLink);
            if (pushMessage == null) {
                continue;
            }

            if (pushMessage.isPersonal() && TextUtils.isEmpty(account)) {
                continue;
            }

            Cursor cursor;
            if (pushMessage.isPersonal()) {
                String sql = "select count(*) from push_message where app_link like ? and account=? and read=?";
                cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{appLink + "%", account, "0"});
            } else {
                String sql = "select count(*) from push_message where app_link like ? and read=?";
                cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{appLink + "%", "0"});
            }

            try {
                cursor.moveToFirst();
                return cursor.getInt(0) > 0;
            } finally {
                cursor.close();
            }
        }

        return false;
    }

    public void save(@NonNull AppLink pushMessage) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            // delete exist appLink and add new one
            database.beginTransaction();
            database.execSQL("DELETE FROM push_message WHERE lower(app_link) = ?",
                    new String[]{pushMessage.getAppLink().toLowerCase(Locale.SIMPLIFIED_CHINESE)});
            database.execSQL("INSERT INTO push_message(title, sub_title, pic_url, app_link, account, update_dt, read) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)", new Object[]{
                    pushMessage.getTitle(),
                    pushMessage.getSubTitle(),
                    pushMessage.getPicUrl(),
                    pushMessage.getAppLink(),
                    pushMessage.getAccount(),
                    pushMessage.getReceiveDt(),
                    pushMessage.isRead()});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void setAsRead(@NonNull String appLink, @NonNull String account) {
        AppLink pushMessage = AppLinkUtils.parseAppLink(dbHelper.getContext(), appLink);
        if (pushMessage == null) {
            return;
        }

        if (pushMessage.isPersonal() && TextUtils.isEmpty(account)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put("read", true);
        values.put("update_dt", System.currentTimeMillis());

        if (pushMessage.isPersonal()) {
            dbHelper.getWritableDatabase().update("push_message", values, "app_link like ? and account=?",
                    new String[]{appLink + "%", account});
        } else {
            dbHelper.getWritableDatabase().update("push_message", values, "app_link like ?",
                    new String[]{appLink + "%"});
        }
    }
}
