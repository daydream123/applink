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
        this(context, null);
    }

    public RedDotView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RedDotView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.red_dot_view);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        PushContentReceiver.register(getContext(), mContentReceiver, false);
        checkVisible();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PushContentReceiver.unregister(getContext(), mContentReceiver);
    }

    /**
     * For some case, push message is private, so need to provide account.
     *
     * @param account account maybe phone number or account id
     */
    public void setAccount(String account) {
        mAccount = account;
    }

    /**
     * Bind push appLink with red dot view, so that it can receive push message automatically.
     */
    public void setAppLinks(String... appLinks) {
        mAppLinks.clear();
        mAppLinks.addAll(Arrays.asList(appLinks));
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

    private void checkVisible() {
        if (mAppLinks == null || mAppLinks.size() == 0) {
            return;
        }

        boolean exist = PushMessageService.getInstance(getContext()).haveUnread(
                mAppLinks, mAccount);
        setVisibility(exist ? View.VISIBLE : GONE);
    }
}