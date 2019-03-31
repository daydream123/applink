package com.feizhang.applink.sample.applink.product;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.feizhang.applink.sample.OrderDetailActivity;
import com.feizhang.applink.sample.applink.DefaultAppLink;

public class OrderDetail extends DefaultAppLink {

    @Override
    public Intent onStartActivity(@NonNull Context context) {
        return new Intent(context, OrderDetailActivity.class);
    }

    @Override
    public boolean isPersonal() {
        return false;
    }
}
