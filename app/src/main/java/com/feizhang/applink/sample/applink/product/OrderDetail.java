package com.feizhang.applink.sample.applink.product;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.feizhang.applink.AppLink;
import com.feizhang.applink.sample.OrderDetailActivity;

public class OrderDetail extends AppLink {

    /**
     * 构建收到appLink打开页面的Intent
     */
    @Override
    public Intent onStartActivity(@NonNull Context context) {
        String orderId = params.get("orderId");
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("orderId", orderId);
        return intent;
    }

    /**
     * 和{@link #onStartActivity(Context)}一并会执行，当收到appLink时候可以做一些额外的工作
     */
    @Override
    public void onExecute(@NonNull Context context) {
        super.onExecute(context);
        Toast.makeText(context, "您的订单有更新", Toast.LENGTH_SHORT).show();
    }

    /**
     * 是否特定账户的信息，对于特定账户的订单推送是需要账户隔离的
     */
    @Override
    public boolean isPrivate() {
        return true;
    }

    /**
     * 是否需要用DB存储，以DB存储是为了下次启动查询到未读状态
     */
    @Override
    public boolean shouldSave() {
        return false;
    }
}
