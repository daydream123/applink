package com.feizhang.applink.sample.applink;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.feizhang.applink.sample.OrderDetailActivity;

public class AppHome extends DefaultAppLink {

    @Override
    public Intent onStartActivity(@NonNull Context context) {
        return new Intent(context, OrderDetailActivity.class);
    }

    @Override
    public void onExecute(@NonNull Context context) {
        super.onExecute(context);
        Toast.makeText(context, "onExecute", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isPersonal() {
        return false;
    }
}
