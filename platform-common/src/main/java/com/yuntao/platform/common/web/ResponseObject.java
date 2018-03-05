package com.yuntao.platform.common.web;

import com.yuntao.platform.common.annotation.ReturnClassComment;
import com.yuntao.platform.common.annotation.ReturnFieldComment;
import com.yuntao.platform.common.constant.SystemConstant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * web返回给前端对象RE
 * Created by shengshan.tang on 2015/12/14 at 21:00
 */
public class ResponseObject implements Serializable {

    @ReturnFieldComment("返回状态,(false 失败;true 成功)")
    private boolean success = true;

    @ReturnFieldComment("消息级别,(info 正常;war 告警;error 错误)")
    private String level = SystemConstant.ResponseLevel.INFO;  //info,warn,error

    @ReturnFieldComment("消息类型,(normal 正常;)")
    private String type = SystemConstant.ResponseType.NORMAL;  //

    @ReturnFieldComment("业务消息类型,(normal 正常;)")
    private String bizType = SystemConstant.ResponseBizType.NORMAL;  //

    @ReturnFieldComment("系统编码,(详见ResponseCode枚举)")
    private String code = SystemConstant.ResponseCode.NORMAL;   //系统定义code

    @ReturnFieldComment("消息内容")
    private String message;

    @ReturnClassComment("业务数据")
    private Object data;



    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Map<String,Object> put(String key, Object value) {
        if(this.data==null || !(this.data instanceof Map)){
            this.data = new HashMap<String,Object>();
        }
        Map<String,Object> map =  (Map<String, Object>) this.data;
        map.put(key, value);
        return map;
    }
}
