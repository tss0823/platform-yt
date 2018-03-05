package com.yuntao.platform.common.log.task;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import com.yuntao.platform.common.CustomizedPropertyConfigurer;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.utils.DateUtil;
import com.yuntao.platform.common.utils.JsonUtils;
import com.yuntao.platform.common.utils.SystemUtil;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 * Created by shengshan.tang on 9/14/2015 at 5:23 PM
 */
public class HbTaskFileOutputStream extends ResilientFileOutputStream {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private String ip = SystemUtil.getLocalIp();

    public HbTaskFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file, append);
    }

    @Override
    public void write(byte[] b) throws IOException {
        HbLogTaskBean logBean = null;
        String message = new String(b,"utf-8");
        Date nowDate = new Date();
//        String time = DateUtil.getFmtYMDHMS(nowDate.getTime());
        if(message.startsWith("bean^|^")) {  //bean 消息类型
            try{
                message = message.substring(7);
                logBean = JsonUtils.json2Object(message,HbLogTaskBean.class);
            }catch (Exception e){
                log.error("logError,message="+message);
                return;
            }
        }else{
            HbLogTaskBean taskLog = HbLogContextMgr.getTaskLog();
            if(taskLog == null){
                return;
            }
            logBean = new HbLogTaskBean();
            logBean.setAppName(taskLog.getAppName());
            logBean.setModule(taskLog.getModule());
            logBean.setBatchNo(taskLog.getBatchNo());
            logBean.setMaster(false);
            logBean.setSuccess(true);
            logBean.setTime(DateUtil.getFmt(nowDate.getTime(),"yyyy-MM-dd HH:mm:ss"));
            logBean.setEndTime(logBean.getTime());
            logBean.setStartTime(logBean.getTime());
            long starTime = System.currentTimeMillis();
            logBean.setStartTimeLong(starTime);
            logBean.setMessage(message);
//            logBean.setDesc(message);
        }
//        logBean.setEndTime(DateUtil.getFmtyMdHmsSSSNoSymbol(nowDate.getTime()));
//        logBean.setTakeTime(nowDate.getTime() - logBean.getStartTimeLong());

        logBean.setIp(ip);
        String model = CustomizedPropertyConfigurer.getModel();
        if(org.apache.commons.lang.StringUtils.equals(model,"dev")){
            model = "test";
        }
        logBean.setType(model);
        String jsonMsg = JsonUtils.object2Json(logBean);
        if(SystemUtils.IS_OS_LINUX){
            jsonMsg += "\n";
        }else if(SystemUtils.IS_OS_MAC){
//            jsonMsg += "\r";
            jsonMsg += "\n";
        }else {
            jsonMsg += "\r\n";
        }
        super.write(jsonMsg.getBytes("utf-8"));
    }

}
