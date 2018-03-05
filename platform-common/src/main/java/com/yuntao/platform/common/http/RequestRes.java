package com.yuntao.platform.common.http;

import java.util.List;
import java.util.Map;

/**
 * Created by shan on 2016/7/22.
 */
public class RequestRes {

    private String url;

    private Map<String, String> headers;

    private Map<String, String> params;

    private List<HttpParam> paramList;

    private String paramText;

    private byte [] paramByte;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getParamText() {
        return paramText;
    }

    public void setParamText(String paramText) {
        this.paramText = paramText;
    }

    public List<HttpParam> getParamList() {
        return paramList;
    }

    public void setParamList(List<HttpParam> paramList) {
        this.paramList = paramList;
    }

    public byte[] getParamByte() {
        return paramByte;
    }

    public void setParamByte(byte[] paramByte) {
        this.paramByte = paramByte;
    }
}
