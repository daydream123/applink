package com.feizhang.applink.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.feizhang.applink.AppLinkUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.openBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), OrderDetailActivity.class));
            }
        });

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appLink = "my-scheme://product/OrderDetail?orderId=abc123";
                AppLinkUtils.pushAppLink(v.getContext(), appLink, "title", "subtitle", null);
            }
        });
    }
}
