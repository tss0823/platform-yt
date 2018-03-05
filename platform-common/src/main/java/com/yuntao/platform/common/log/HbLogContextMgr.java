package com.yuntao.platform.common.log;

import com.yuntao.platform.common.CustomizedPropertyConfigurer;
import com.yuntao.platform.common.exception.AuthException;
import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.log.task.HbLogTaskBean;
import com.yuntao.platform.common.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by shengshan.tang on 9/14/2015 at 5:48 PM
 */
public class HbLogContextMgr {

    private final static Logger log = LoggerFactory.getLogger(HbLogContextMgr.class);
    private final static Logger stackLog = LoggerFactory.getLogger("stackLog");
    private final static Logger taskLog = LoggerFactory.getLogger("taskLog");

    private static String ip = SystemUtil.getLocalIp();

    private static boolean isMatchApp = false;

    private static boolean isOfferLog = true;

    private static String appName = null;

    private static ThreadLocal<LogContext> statckThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<HbLogTaskBean> taskThreadLocal = new ThreadLocal<>();

    private static Queue<HbLogBean> logQueue = new ConcurrentLinkedQueue<HbLogBean>();

    public static void errorLog(Throwable e){
        if(!isMatchApp){
            return;
        }
        LogContext context = getLogContext();
        if(context == null){
            return;
        }
        if( e instanceof BizException || e instanceof AuthException){
            context.setLogLevel(LogContext.LogLevel.BIZ_ERROR);
        }else{
            context.setLogLevel(LogContext.LogLevel.SYS_ERROR);
        }
        String errResultMsg = "errr msg:\r\n";
        String errMsg = ExceptionUtils.getPrintStackTrace(e);
        errResultMsg += errMsg;
        stackLog.info(errResultMsg);
    }


    public static LogContext getLogContext(){
        return statckThreadLocal.get();
    }

    public static void setLogContext(LogContext logContext){
        statckThreadLocal.set(logContext);
    }

    public static void addBodyMsg(String message){
        LogContext logContext = statckThreadLocal.get();
        if(logContext != null){
            logContext.addMessage(message);
        }
        stackLog.info(message);
    }
    public static String getStackId(){
        return statckThreadLocal.get().getStackId();
    }

    private static String genStatckId(){
        StringBuilder sb = new StringBuilder();
        sb.append(appName);
        sb.append("-");
        sb.append(ip);
        sb.append("-");
        sb.append(new Date().getTime());
        sb.append(RandomStringUtils.random(4,false,true));
        return sb.toString();

    }

    public static void startStack(HttpServletRequest request){
        if(!isMatchApp || !isMatchUrl(request)){
            return;
        }
        LogContext context = new LogContext(genStatckId());
        context.setAppName(appName);
        Date date = new Date();
        context.setStartTimeMs(date.getTime());
        context.setStartTime(DateUtil.getFmtyMdHmsSSSNoSymbol(date.getTime()));
        context.setLogLevel(LogContext.LogLevel.INFO);
        context.setUrl(request.getRequestURI());
        statckThreadLocal.set(context);

    }

    public static void appendParameter(HttpServletRequest request){
        LogContext context = getLogContext();
        if(context == null){
            return;
        }
        Map<String,String> parameterMap = new LinkedMap<>();
//        JSONObject parameterJson  = new JSONObject();
        Enumeration<String> parameterNames = request.getParameterNames();
        Iterator<String> fileNames = null;
        String contentType = request.getHeader("content-type");
        if (StringUtils.startsWith(contentType, "multipart/form-data")) {
            if (request instanceof DefaultMultipartHttpServletRequest) {
                parameterNames = ((DefaultMultipartHttpServletRequest) request).getParameterNames();
                fileNames = ((DefaultMultipartHttpServletRequest) request).getFileNames();
            }
        }else if(!StringUtils.equalsIgnoreCase(contentType,"application/x-www-form-urlencoded")){
//            try {
//                List<String> paramList = IOUtils.readLines(request.getInputStream());
//                parameterMap.put("value",StringUtils.join(paramList));
//            } catch (IOException e) {
//            }
        }
        String parameterName = null;
        String[] parameterValues = null;
        while (parameterNames != null && parameterNames.hasMoreElements()) {
            parameterName = parameterNames.nextElement();
            parameterValues = request.getParameterValues(parameterName);
            String parameterValueArray = "";
            if (parameterValues != null && parameterValues.length > 0) {
                for (String parameterValue : parameterValues) {
                    parameterValueArray += parameterValue + ",";
                }
            }
            if(parameterValueArray.length() > 0){
                parameterValueArray = parameterValueArray.substring(0,parameterValueArray.length()-1);
            }
            parameterMap.put(parameterName,parameterValueArray);
        }


        //

        if (fileNames != null) {
            while (fileNames.hasNext()){
                parameterMap.put(fileNames.next(),"");
            }
        }
        String parameterJson = JsonUtils.object2Json(parameterMap);
        context.setParameters(parameterJson);

        if(StringUtils.isEmpty(context.getMobile())){
            Object mobileObj = parameterMap.get("mobile");
            if(mobileObj == null || mobileObj.toString().equals("")){
                mobileObj = parameterMap.get("accountNo");
            }
            String mobile = mobileObj != null ? mobileObj.toString() : null;
            context.setMobile(mobile);
        }

    }

    public static void endStack(HttpServletRequest request, HttpServletResponse response){
        if(!isMatchApp || !isMatchUrl(request)){
            return;
        }
        LogContext context = getLogContext();
        if(context == null){
            return;
        }
        HbLogBean hbLogBean  = new HbLogBean();
        hbLogBean.setMaster(true);

        //处理 header request
        hbLogBean.setReqUrl(request.getRequestURL().toString());

        //req headers
        Map<String,String> headerMap = new HashMap<>() ;

        Enumeration<String> headerNames = request.getHeaderNames();

        String headName = null;
        Enumeration<String> headValues = null;
        while (headerNames != null && headerNames.hasMoreElements()) {
            headName = headerNames.nextElement();
            headValues = request.getHeaders(headName);
            String headValueArray = "";
            while (headValues != null && headValues.hasMoreElements()) {
                headValueArray += headValues.nextElement() + ",";
            }
            if(headValueArray.length() > 0){
                headValueArray = headValueArray.substring(0,headValueArray.length()-1);
            }
            headerMap.put(headName,headValueArray);
        }

        String headerJson = JsonUtils.object2Json(headerMap);
        hbLogBean.setReqHeaders(headerJson);


        //req parameters
        if(StringUtils.isEmpty(context.getParameters())){
            appendParameter(request);
        }
        hbLogBean.setParameters(context.getParameters());
        //end

        int status = response.getStatus();
        // ! (status >= 200 && status < 400)
        if(!(status >= HttpServletResponse.SC_OK && status <= HttpServletResponse.SC_BAD_REQUEST)){
            context.setLogLevel(LogContext.LogLevel.SYS_ERROR);
        }
        context.setStatus(status);
        String key = "user-agent";
        hbLogBean.setUserAgent(request.getHeader(key));
        key = "content-length";
        String contentLen = request.getHeader(key);
        long reqContentLen = NumberUtil.getNumber(contentLen);
        hbLogBean.setReqContentLen(reqContentLen);
        String clientIp = StringUtils.isBlank(request.getHeader("X-real-ip")) ? request.getRemoteAddr() : request.getHeader("X-real-ip");
        hbLogBean.setClientIp(clientIp);

        String resStr = "";
        Integer resContentLength = 0;
        if(StringUtils.isNotEmpty(context.getRespString())){
            resContentLength = context.getRespString().length();
            resStr =  context.getRespString();
        }
        hbLogBean.setResponse(resStr);
        try{
            Collection<String> resHeaderNames = response.getHeaderNames();
            if(CollectionUtils.isNotEmpty(resHeaderNames)){
                headerMap.clear();
                for(String headerName : resHeaderNames){
                    Collection<String> headers = response.getHeaders(headerName);  //多能一样的有多个，比如cookie,可以有N个。
                    Iterator<String> iterator = headers.iterator();

                    int index = 0;
                    String newHeaderName = headerName;
                    while(iterator.hasNext()){
                        headerMap.put(newHeaderName,iterator.next());
                        index++;
                        newHeaderName = headerName+index;
                    }
                }
                long resContentLen = NumberUtil.getNumber(resContentLength);
                hbLogBean.setResContentLen(resContentLen);
                headerJson = JsonUtils.object2Json(headerMap);
                hbLogBean.setResHeaders(headerJson);
            }

            context.setEnd(true);
            String json = JsonUtils.object2Json(hbLogBean);
            json = "master^|^" + json;
            stackLog.info(json);
        }catch (Exception e){
        }
    }

    public static void setUser(Long userId,String mobile,String name){
        if(!isMatchApp){
            return;
        }
        LogContext context = getLogContext();
        if(context == null){
            return;
        }
        context.setUserId(userId);
        context.setMobile(mobile);
        context.setName(name);
        statckThreadLocal.set(context);
    }

    public static void setResponse(Object respString){
        if(respString == null){
            return;
        }
//        stackLog.info("mark reponse start2");
        if(!isMatchApp){
            return;
        }
//        stackLog.info("mark reponse start3");
        LogContext context = getLogContext();
        if(context == null){
            return;
        }
//        stackLog.info("mark reponse start4");
        context.setRespString(JsonUtils.object2Json(respString));
//        stackLog.info("mark reponse end");
        statckThreadLocal.set(context);
    }

    public static void offerLog(HbLogBean bean){
        if(!isMatchApp || !isOfferLog){
            return;
        }
        bean.setAppName(appName);
        logQueue.offer(bean);
    }

    public static List<HbLogBean> [] dispatchLog(){
        if(logQueue.isEmpty()){
            return null;
        }
        List<HbLogBean> masterLogList = new ArrayList<HbLogBean>();
        List<HbLogBean> msgLogList = new ArrayList<HbLogBean>();
        int size = logQueue.size();
        int len = 0;
        if(!isOfferLog && size < 500){  //open
            isOfferLog = true;
            log.info("offer log open!");
        }
        if(size <= 20){
            len = size;
        }else if(size > 20 && size <= 100){
            len = (int)(size * 0.8);
        }else if(size > 100 && size <= 5000){
            len = 60;
        }else if(size > 5000 && size <= 10000){  //丢弃部分
            int takeLen = (int)(size * 0.2);
            while(takeLen > 0){
                logQueue.poll();
                takeLen--;
            }
            len = 60;
        }else{  //大于10000，自动关闭offer log queue
            len = 60;
            isOfferLog = false;
            //log.info("offer log closed!");
        }
        while(len > 0 && !logQueue.isEmpty()){
            HbLogBean logBean = logQueue.poll();
            if(logBean.isMaster()){
                masterLogList.add(logBean);
            }
            msgLogList.add(logBean);
            len--;
        }
        //log.info("current logQueue size=" + size + ",get real master log size=" + masterLogList.size()+",msg log size="+msgLogList.size());
        return new List []{masterLogList,msgLogList};
    }


    public static void initData(){
        String stackLog = CustomizedPropertyConfigurer.getContextProperty("stackLog");
        isMatchApp = Boolean.valueOf(stackLog);
        appName = CustomizedPropertyConfigurer.getContextProperty("appName");
    }

    public static boolean isMatchApp(){
        return isMatchApp;
    }

    public static String getAppName(){
        return appName;
    }

    public static String getIp(){
        return ip;
    }

    static List<String> excluedUrls = new ArrayList<>();
    static {
        excluedUrls.add("/log/");
    }

    public static boolean isMatchUrl(HttpServletRequest request){
        String url = request.getRequestURI();
        if(StringUtils.indexOf(url, "_resources") != -1){  //静态文件
            return false;
        }
        //业务url
        if(StringUtils.indexOf(url, "/resUpload") != -1){
            return false;
        }
        if(StringUtils.indexOf(url, "/proxy/recordShop") != -1){
            return false;
        }
        //end
        if(StringUtils.endsWithIgnoreCase(request.getRequestURL(), "checkServerStatus")){
            return false;     //过滤心跳
        }
        //过滤自定义
        for(String excludeUlr : excluedUrls){
            if(url.contains(excludeUlr)){
                return false;
            }
        }

        return true;
    }

    public static void writeSuccesMsg(String appName,String module,String batchNo,String message,String descMsg){
        HbLogTaskBean hbLogTaskBean = new HbLogTaskBean();
        hbLogTaskBean.setAppName(appName);
        hbLogTaskBean.setModule(module);
        hbLogTaskBean.setBatchNo(batchNo);
        hbLogTaskBean.setMessage(message);
        hbLogTaskBean.setDesc(descMsg);
        hbLogTaskBean.setMaster(false);
        Date nowDate = new Date();
        hbLogTaskBean.setTime(DateUtil.getFmt(nowDate.getTime(),"yyyy-MM-dd HH:mm:ss"));
        hbLogTaskBean.setStartTime(hbLogTaskBean.getTime());
        hbLogTaskBean.setStartTimeLong(nowDate.getTime());
        hbLogTaskBean.setSuccess(true);
        writeTaskMsg(hbLogTaskBean);
    }

    public static void writeSuccesMsg(String appName,String module,String batchNo,String message){
        writeSuccesMsg(appName,module,batchNo,message,"");
    }

    public static void writeErrorMsg(String appName,String module,String batchNo,String message,String errMsg){
        Date nowDate = new Date();
        HbLogTaskBean hbLogTaskBean = new HbLogTaskBean();
        hbLogTaskBean.setAppName(appName);
        hbLogTaskBean.setModule(module);
        hbLogTaskBean.setBatchNo(batchNo);
        hbLogTaskBean.setMessage(message);
        hbLogTaskBean.setMaster(false);
        hbLogTaskBean.setTime(DateUtil.getFmt(nowDate.getTime(),"yyyy-MM-dd HH:mm:ss"));
        hbLogTaskBean.setStartTime(hbLogTaskBean.getTime());
        hbLogTaskBean.setStartTimeLong(nowDate.getTime());
        hbLogTaskBean.setSuccess(false);
        hbLogTaskBean.setDesc(errMsg);
        writeTaskMsg(hbLogTaskBean);
    }

    public static void writeMasterMsg(String appName,String module,String batchNo,Long time,String message,boolean success,String errMsg){
        HbLogTaskBean hbLogTaskBean = new HbLogTaskBean();
        hbLogTaskBean.setAppName(appName);
        hbLogTaskBean.setModule(module);
        hbLogTaskBean.setBatchNo(batchNo);
        hbLogTaskBean.setMessage(message);
        hbLogTaskBean.setMaster(true);
        hbLogTaskBean.setStartTimeLong(time);
        hbLogTaskBean.setTime(DateUtil.getFmt(time,"yyyy-MM-dd HH:mm:ss"));
        hbLogTaskBean.setStartTime(hbLogTaskBean.getTime());
        hbLogTaskBean.setSuccess(success);
        hbLogTaskBean.setDesc(errMsg);
        writeTaskMsg(hbLogTaskBean);
    }

    public static void startTask(String module,String message){
        String appName = AppConfigUtils.getAppName();
        long starTime = System.currentTimeMillis();
        String batchNo = starTime + RandomStringUtils.random(4, false, true);
        HbLogTaskBean hbLogTaskBean = new HbLogTaskBean();
        hbLogTaskBean.setAppName(appName);
        hbLogTaskBean.setModule(module);
        hbLogTaskBean.setBatchNo(batchNo);
        hbLogTaskBean.setMaster(true);
        hbLogTaskBean.setMessage(message);
        hbLogTaskBean.setStartTimeLong(starTime);
        hbLogTaskBean.setTime(DateUtil.getFmt(starTime,"yyyy-MM-dd HH:mm:ss"));
        hbLogTaskBean.setStartTime(hbLogTaskBean.getTime());
//        hbLogTaskBean.setSuccess(true);
        taskThreadLocal.set(hbLogTaskBean);
    }

    public static void endTask(boolean state,String desMsg){
        Date nowDate = new Date();
        HbLogTaskBean hbLogTaskBean = taskThreadLocal.get();
        hbLogTaskBean.setSuccess(state);
        hbLogTaskBean.setDesc(desMsg);
        hbLogTaskBean.setEndTime(DateUtil.getFmtyMdHmsSSSNoSymbol(nowDate.getTime()));
        hbLogTaskBean.setTakeTime(nowDate.getTime() - hbLogTaskBean.getStartTimeLong());
        writeTaskMsg(hbLogTaskBean);
        taskThreadLocal.remove();
    }

    private static void writeTaskMsg(HbLogTaskBean hbLogTaskBean){
        String json = JsonUtils.object2Json(hbLogTaskBean);
        if (StringUtils.isNotEmpty(json)) {
            taskLog.info("bean^|^"+json);
        }
    }

    public static HbLogTaskBean getTaskLog(){
        return taskThreadLocal.get();
    }


    public static void main(String[] args) {
        System.out.println(new Date().getTime()+ RandomStringUtils.random(4,false,true));
    }


}
