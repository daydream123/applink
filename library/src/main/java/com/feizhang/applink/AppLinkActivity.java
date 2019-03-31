package com.feizhang.applink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zhangfei on 2015/9/7.
 *
 * It's a activity to be launched by scheme,
 * it should be registered in your App manifest with your scheme.
 */
public class AppLinkActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE_ID = "messageId";
    public static final String EXTRA_APP_LINK = "appLink";

    private String mAppLink;

    public static Intent buildIntent(@NonNull Context context,
                                     @NonNull AppLink appLink,
                                     @NonNull String account) {
        Intent intent = new Intent(context, AppLinkActivity.class);
        intent.putExtra(EXTRA_MESSAGE_ID, appLink.getId());
        intent.putExtra(EXTRA_APP_LINK, appLink.getAppLink());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAppLink = savedInstanceState.getString(EXTRA_APP_LINK);
        } else {
            mAppLink = getIntent().getStringExtra(EXTRA_APP_LINK);
        }

        if (!TextUtils.isEmpty(mAppLink)) {
            AppLinkUtils.redirect(this, mAppLink);
            finish();
            return;
        }

        Intent intent = getIntent();
        if (TextUtils.isEmpty(intent.getScheme()) || TextUtils.isEmpty(intent.getDataString())) {
            Log.e(AppLink.TAG, "empty scheme or dataString");
            finish();
            return;
        }

        if (AppLinkUtils.isSameScheme(intent.getScheme())) {
            String dataString = intent.getDataString();
            if (dataString != null){
                AppLinkUtils.redirect(this, dataString, true);
            }
        }

        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_APP_LINK, mAppLink);
    }
}
