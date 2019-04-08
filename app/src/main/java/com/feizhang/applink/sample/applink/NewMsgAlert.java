package com.feizhang.applink.sample.applink;

import com.feizhang.applink.AppLink;

public class NewMsgAlert extends AppLink {

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public boolean shouldSave() {
        return true;
    }
}
