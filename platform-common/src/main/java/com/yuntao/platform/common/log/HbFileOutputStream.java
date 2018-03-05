package com.yuntao.platform.common.log;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import com.yuntao.platform.common.CustomizedPropertyConfigurer;
import com.yuntao.platform.common.profiler.ProfileTaskManger;
import com.yuntao.platform.common.utils.DateUtil;
import com.yuntao.platform.common.utils.JsonUtils;
import com.yuntao.platform.common.utils.SystemUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by shengshan.tang on 9/14/2015 at 5:23 PM
 */
public class HbFileOutputStream extends ResilientFileOutputStream {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private String ip = SystemUtil.getLocalIp();

    public HbFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file, append);
    }

    @Override
    public void write(byte[] b) throws IOException {
        //获取日志栈key
        LogContext logContext = HbLogContextMgr.getLogContext();
        if(logContext == null){
            return;
        }
        String stackId = logContext.getStackId();
//        StringBuilder sb = new StringBuilder();
//        sb.append(time);
//        sb.append("^|^");
//        sb.append(stackId);
//        sb.append("^|^");
        String mobile = logContext.getMobile();
        Long userId = logContext.getUserId();
//        sb.append(mobile);
//        sb.append("^|^");
//        sb.append(userId);
//        sb.append("^|^");
//        sb.append(logContext.getUrl());
//        sb.append("^|^");

        //set bean
        HbLogBean logBean = null;
        String message = new String(b,"utf-8");
        Date nowDate = new Date();
        String time = DateUtil.getFmtYMDHMS(nowDate.getTime());
        if(message.startsWith("master^|^")){  //master 主体消息
            try{
                message = message.substring(9);
                logBean = JsonUtils.json2Object(message,HbLogBean.class);
            }catch (Exception e){
                log.error("logError,message="+message);
                return;

            }
//            logBean.setMaster(true);
            logBean.setEndTime(DateUtil.getFmtyMdHmsSSSNoSymbol(nowDate.getTime()));
            logBean.setTakeTime(nowDate.getTime() - logContext.getStartTimeMs());

            //处理body message
            List<String> messageList = logContext.getMessage();
            if (CollectionUtils.isNotEmpty(messageList)) {
                String msg = StringUtils.join(messageList, "^#^");
                logBean.setMessage(msg);
            }

        }else{
            if(!message.startsWith("methodStack^|^")){
                String threadKey = ProfileTaskManger.getThreadKey();
                message = threadKey+"^|^"+message;
            }
            logBean  = new HbLogBean();
            logBean.setMessage(message);

        }

        logBean.setAppName(logContext.getAppName());
        logBean.setTime(time);
        logBean.setTimeLong(nowDate.getTime());
        logBean.setStackId(stackId);
        logBean.setUserId(userId);
        logBean.setMobile(mobile);
        logBean.setName(logContext.getName());
        logBean.setUrl(logContext.getUrl());
//        byte compressByte [] = CompressUtils.compress(message);
//        logBean.setMessage(message);
        logBean.setIp(ip);
        logBean.setStatus(logContext.getStatus());
        logBean.setStartTime(logContext.getStartTime());
        String model = CustomizedPropertyConfigurer.getModel();
        if(org.apache.commons.lang.StringUtils.equals(model,"dev")){
            model = "test";
        }
        logBean.setType(model);
        logBean.setLevel(logContext.getLogLevel().name());
//        HbLogContextMgr.offerLog(logBean);

        //合并byte
//        byte [] destByte = sb.toString().getBytes();
//        int byteLen = b.length + destByte.length+2;
//        byte resultByte [] = new byte[byteLen];
//        try{
//            System.arraycopy(destByte,0,resultByte,0,destByte.length);
//            System.arraycopy(b,0,resultByte,destByte.length,b.length);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        resultByte[byteLen-2] = 13;  // /r
//        resultByte[byteLen-1] = 10;  // /n
        String jsonMsg = JsonUtils.object2Json(logBean);
        if(SystemUtils.IS_OS_LINUX){
            jsonMsg += "\n";
        }else if(SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX){
//            jsonMsg += "\r";
            jsonMsg += "\n";
        }else {
            jsonMsg += "\r\n";
        }
        super.write(jsonMsg.getBytes("utf-8"));
    }

}
