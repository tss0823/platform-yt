package com.yuntao.platform.common.support;

import com.yuntao.platform.common.constant.SystemConstant;
import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.http.HttpNewUtils;
import com.yuntao.platform.common.http.RequestRes;
import com.yuntao.platform.common.http.ResponseRes;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.utils.AppConfigUtils;
import com.yuntao.platform.common.utils.DateUtil;
import com.yuntao.platform.common.utils.ExceptionUtils;
import com.yuntao.platform.common.utils.MD5Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shan on 2016/8/20.
 */
public class SendMsgUtils {

    private static String accountId = "";
    private static String token = "";
    private static String appId = "";

    private static int tryMaxCount = 3;

    private static String smsKey = "";

    /**
     * 初始化,配置参数
     *
     * @param initMaps
     */
    public static void init(Map<String, String> initMaps) {
        accountId = initMaps.get("sms.accountId");
        token = initMaps.get("sms.token");
        appId = initMaps.get("sms.appId");
        smsKey = initMaps.get("sms.key");
    }


    public static void sendSMS2(final Long templateId, final String mobile, final List<String> msgList) {
        new Thread(new Runnable() {
            String appName = AppConfigUtils.getAppName();
            long actionTime = System.currentTimeMillis();
            String logTitle = "send sms finished! mobile=" + mobile + ",msg=" + StringUtils.join(msgList, "^#^");

            @Override
            public void run() {
                try {
                    RequestRes requestRes = new RequestRes();
                    Map<String, String> headers = new HashMap();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json;charset=utf-8");
                    StringBuilder sb = new StringBuilder("https://app.cloopen.com:8883");
                    String accountSid = accountId;
                    StringBuilder sigSb = new StringBuilder(accountSid);
                    sigSb.append(token);
                    String time = DateUtil.getFmt(new Date().getTime(), "yyyyMMddHHmmss");
                    sigSb.append(time);
                    String sig = MD5Util.MD5Encode(sigSb.toString());
                    sb.append("/2013-12-26/Accounts/" + accountSid + "/SMS/TemplateSMS?sig=");
                    sb.append(sig);
                    requestRes.setUrl(sb.toString());
                    byte[] authBytes = Base64.encodeBase64(new String(accountSid + ":" + time).getBytes());
                    headers.put("Authorization", new String(authBytes));
                    requestRes.setHeaders(headers);
                    String datas = "";
                    if (CollectionUtils.isNotEmpty(msgList)) {
                        datas = "\"" + StringUtils.join(msgList, "\",\"") + "\"";
                    }
                    String paramText = "{\"to\":\"" + mobile + "\",\"appId\":\"" + appId + "\",\"templateId\":\"" + templateId + "\",\"datas\":[" + datas + "]}";
                    requestRes.setParamText(paramText);
                    ResponseRes execute = HttpNewUtils.execute(requestRes);
                    byte[] result = execute.getResult();
                    String s = new String(result);
                    HbLogContextMgr.writeMasterMsg(appName, "sms", "sendMsg", actionTime, logTitle, true, s);
                } catch (Exception e) {
                    HbLogContextMgr.writeMasterMsg(appName, "sms", "sendMsg", actionTime, logTitle, false, ExceptionUtils.getPrintStackTrace(e));
                    if (e instanceof BizException) {
                        BizException bizException = (BizException) e;
                        if (bizException.getCode() == SystemConstant.ExceptionCode.REMOTE_TIME_OUT) {
                            //add to try send queue
//                            String sentMsgListKey = CacheConstant + "_" + SystemUtil.getLocalIp();
//                            queueService.add(sentMsgListKey, message);
                        }
                    }
                }

            }
        }).start();


    }

    public static String sendSMSTryError(final Long templateId, final String mobile, final List<String> msgList,int tryCount) {
        try{
            String result = sendSMS(templateId, mobile, msgList);
            System.out.printf("result="+result);
        }catch (Exception e){
            if (e instanceof BizException) {
                BizException bizException = (BizException) e;
                if (bizException.getCode() == SystemConstant.ExceptionCode.REMOTE_TIME_OUT) {
                    if(tryCount < tryMaxCount){  // 最多尝试3次
                        //add to try send queue
                        sendSMSTryError(templateId,mobile,msgList,tryCount+1);
                    }else{
                        System.err.printf("over tryMaxCount,give up");
                    }
                }
            }

        }
        return null;
    }

    /**
     * 聚合发送
     * @param templateId
     * @param mobile
     * @param value
     * @return
     */
    public static String sendSMSJH(final Long templateId, final String mobile, final String value) {
        ResponseRes responseRes = HttpNewUtils.get("http://v.juhe.cn/sms/send?mobile=" + mobile + "&tpl_id=" + templateId + "&tpl_value=" + value + "&dtype=&key=" + smsKey);
        return responseRes.getBodyText();
    }
    public static String sendSMS(final Long templateId, final String mobile, final List<String> msgList) {
        RequestRes requestRes = new RequestRes();
        Map<String, String> headers = new HashMap();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json;charset=utf-8");
        StringBuilder sb = new StringBuilder("https://app.cloopen.com:8883");
        String accountSid = accountId;
        StringBuilder sigSb = new StringBuilder(accountSid);
        sigSb.append(token);
        String time = DateUtil.getFmt(new Date().getTime(), "yyyyMMddHHmmss");
        sigSb.append(time);
        String sig = MD5Util.MD5Encode(sigSb.toString());
        sb.append("/2013-12-26/Accounts/" + accountSid + "/SMS/TemplateSMS?sig=");
        sb.append(sig);
        requestRes.setUrl(sb.toString());
        byte[] authBytes = Base64.encodeBase64(new String(accountSid + ":" + time).getBytes());
        headers.put("Authorization", new String(authBytes));
        requestRes.setHeaders(headers);
        String datas = "";
        if (CollectionUtils.isNotEmpty(msgList)) {
            datas = "\"" + StringUtils.join(msgList, "\",\"") + "\"";
        }
        String paramText = "{\"to\":\"" + mobile + "\",\"appId\":\"" + appId + "\",\"templateId\":\"" + templateId + "\",\"datas\":[" + datas + "]}";
        requestRes.setParamText(paramText);
        ResponseRes execute = HttpNewUtils.execute(requestRes);
        byte[] result = execute.getResult();
        return new String(result);
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
//                /2013-12-26/Accounts/abcdefghijklmnopqrstuvwxyz012345/SMS/TemplateSMS?sig=
//                C1F20E7A9733CE94F680C70A1DBABCDE
        String encode = URLEncoder.encode("#day#=2017-11/30&#time#=12:55 - 13:55&#courseName#=减脂训练","utf-8");
        System.out.print("format="+encode+"");


        accountId = "8a216da85a7d6742015a9e7013ae0d15";
        token = "ecf5cd2c9da9404e9a45054d5dc0c2a9";
        appId = "8a216da85a7d6742015a9e70166d0d1b";
        smsKey = "a0305596b0cf96c44ff92cdebfece3a9";
        sendSMSJH(53929L, "15267164682", encode);
    }
}
