package com.feizhang.applink.sample.applink;

import com.feizhang.applink.AppLink;
import com.feizhang.applink.sample.R;

public abstract class DefaultAppLink extends AppLink {

    @Override
    public int getSmallIcon() {
        return R.drawable.nofication_small_ic;
    }
}
