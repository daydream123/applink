package com.feizhang.applink.sample.applink;

import com.feizhang.applink.AppLink;

public class NewMsgAlert extends AppLink {

    @Override
    public boolean isPersonal() {
        return true;
    }

    @Override
    public boolean needSave() {
        return true;
    }
}
