package com.feizhang.applink;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

/**
 * Created by zhangfei on 2015/9/8.
 */
public class PushMessageDbHelper extends SQLiteOpenHelper {
    private Context mContext;

    PushMessageDbHelper(@NonNull Context context) {
        super(context, "push_message.db", null, 1);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS push_message (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "sub_title TEXT," +
                "pic_url TEXT," +
                "app_link TEXT," +
                "account TEXT," +
                "update_dt TEXT," +
                "read INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @NonNull
    Context getContext() {
        return mContext;
    }
}
