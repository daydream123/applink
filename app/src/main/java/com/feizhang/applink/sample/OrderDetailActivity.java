package com.feizhang.applink.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.feizhang.applink.AppLink;
import com.feizhang.applink.AppLinkUtils;
import com.feizhang.applink.PushContentReceiver;
import com.feizhang.applink.RedDotView;

import java.util.Collections;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    private RedDotView mRedDotView;

    private PushContentReceiver mReceiver = new PushContentReceiver() {

        @Override
        public List<String> getAppLinks() {
            return Collections.singletonList("my-scheme://product/OrderDetail");
        }

        @Override
        public String getAccount(@NonNull Context context) {
            return MyApplication.accountId;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onReceive(@NonNull Context context, @NonNull AppLink appLink) {
            Toast.makeText(context, "订单已刷新", Toast.LENGTH_SHORT).show();
            // return true indicates the receiver will not deliver to other receivers.
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        TextView newsText = findViewById(R.id.alertText);
        newsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRedDotView.remove();
            }
        });

        mRedDotView = findViewById(R.id.redDotView);
        mRedDotView.init(MyApplication.accountId, "my-scheme://NewMsgAlert");

        findViewById(R.id.sendOrderBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink = "my-scheme://product/OrderDetail?orderId=abc123";
                AppLinkUtils.pushAppLink(v.getContext(), appLink);
            }
        });

        findViewById(R.id.sendNewMsgBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink = "my-scheme://NewMsgAlert";
                AppLinkUtils.pushAppLink(v.getContext(), appLink);
            }
        });

        PushContentReceiver.register(this, mReceiver, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PushContentReceiver.unregister(this, mReceiver);
    }
}
