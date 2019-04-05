package com.feizhang.applink.sample.applink.product;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.feizhang.applink.AppLink;
import com.feizhang.applink.sample.OrderDetailActivity;

public class OrderDetail extends AppLink {

    @Override
    public Intent onStartActivity(@NonNull Context context) {
        return new Intent(context, OrderDetailActivity.class);
    }

    @Override
    public boolean isPersonal() {
        return false;
    }

    @Override
    public boolean needSave() {
        return false;
    }
}
