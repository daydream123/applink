package com.feizhang.applink;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangfei on 2016/1/19.
 */
public class RedDotView extends AppCompatImageView {
    private String mAccount = "no-account";

    private List<String> mAppLinks = new ArrayList<>();

    private PushContentReceiver mContentReceiver = new PushContentReceiver() {

        @Override
        public String getAccount(@NonNull Context context) {
            return mAccount;
        }

        @Override
        public List<String> getAppLinks() {
            return mAppLinks;
        }

        @Override
        public boolean onReceive(@NonNull Context context, @NonNull AppLink appLink) {
            Log.d("DotView", "onReceive appLink: " + appLink);

            if (mAppLinks == null || mAppLinks.size() == 0) {
                return false;
            }

            boolean haveUnReadMsg = false;
            for (String item : mAppLinks) {
                if (PushMessageService.getInstance(getContext()).haveUnread(item, getAccount(context))) {
                    haveUnReadMsg = true;
                    break;
                }
            }

            setVisibility(haveUnReadMsg ? VISIBLE : GONE);
            return false;
        }
    };

    public RedDotView(@NonNull Context context) {
        super(context);
        initView();
    }

    public RedDotView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RedDotView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setImageResource(R.drawable.red_dot_view);
        if (isInEditMode()) {
            return;
        }

        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewAttachedToWindow(View v) {
                PushContentReceiver.register(v.getContext(), mContentReceiver, false);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                PushContentReceiver.unregister(v.getContext(), mContentReceiver);
            }
        });
    }

    /**
     * Bind push appLink with red dot view, so that it can receive push message automatically.
     *
     * @param account  it maybe account id or phone number
     * @param appLinks appLinks current red dot view will receive
     */
    public void init(String account, String... appLinks) {
        mAccount = account;
        mAppLinks.clear();
        mAppLinks.addAll(Arrays.asList(appLinks));
        initVisibility();
    }

    /**
     * Same as {@link #init(String, String...)} but no account
     * @param appLinks appLinks current red dot view will receive
     */
    public void init(String... appLinks) {
        mAppLinks.clear();
        mAppLinks.addAll(Arrays.asList(appLinks));
        initVisibility();
    }

    /**
     * Remove red dot
     */
    public void remove() {
        if (mAppLinks == null || mAppLinks.size() == 0) {
            return;
        }

        // remove message when clicked
        for (String appLink : mAppLinks) {
            PushMessageService.getInstance(getContext()).delete(appLink, mAccount);
            AppLinkUtils.refreshAppLink(getContext(), appLink);
        }
    }

    private void initVisibility() {
        if (mAppLinks == null || mAppLinks.size() == 0) {
            return;
        }

        boolean exist = PushMessageService.getInstance(getContext()).haveUnread(
                mAppLinks, mAccount);
        setVisibility(exist ? View.VISIBLE : GONE);
    }
}