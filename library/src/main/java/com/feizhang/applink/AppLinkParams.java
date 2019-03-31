package com.feizhang.applink;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangfei on 2015/9/8.
 *
 */
public class AppLinkParams {
    private String className;

    private String jsonStr;
    private Map<String, String> params = new HashMap<>();

    public AppLinkParams(String className, Map<String, String> params) {
        this.className = className;
        this.params.clear();
        this.params.putAll(params);
    }

    public AppLinkParams(String className, String jsonStr) {
        this.className = className;
        this.jsonStr = jsonStr;
    }

    public AppLinkParams(String className) {
        this(className, new HashMap<String, String>());
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getJsonStr() {
        return jsonStr;
    }
}

