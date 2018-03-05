package com.yuntao.platform.common.log;

import com.alibaba.dubbo.common.utils.Log;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shengshan.tang on 9/14/2015 at 9:06 PM
 */
public class LogContext {

    public enum LogLevel{
        SYS_ERROR,BIZ_ERROR,INFO
    }

    public LogContext(){}

    private Long userId;

    private String mobile;

    private String name;

    private String stackId;

    private String url;

    private LogLevel logLevel;

    private int status;

    private long startTimeMs;

    private String startTime;

    private String endTime;

    private String respString;

    private String parameters;

    private boolean isEnd;

    private String appName;

    private List<String> message = new ArrayList();

    public LogContext(String stackId) {
        this.stackId = stackId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStackId() {
        return stackId;
    }

    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRespString() {
        return respString;
    }

    public void setRespString(String respString) {
        this.respString = respString;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public List<String> getMessage() {
        return message;
    }

    public void addMessage(String message) {
        this.message.add(message);
    }

}
