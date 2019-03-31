package com.feizhang.applink.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.feizhang.applink.AppLink;
import com.feizhang.applink.AppLinkUtils;
import com.feizhang.applink.PushContentReceiver;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView mTextView;

    private PushContentReceiver mReceiver = new PushContentReceiver() {
        @Override
        public String[] getAppLinksToCompare() {
            return new String[]{"my-scheme://product/OrderDetail"};
        }

        @Override
        public String getAccount(@NonNull Context context) {
            return MyApplication.accountId;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onReceive(@NonNull Context context, @NonNull AppLink appLink) {
            mTextView.setText("received appLink: " + appLink.getAppLink());

            // return true indicates the receiver will not deliver to other receivers.
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        mTextView = findViewById(R.id.pushContentText);

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink = "my-scheme://product/OrderDetail?orderId=abc123";
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
