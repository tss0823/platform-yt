package com.yuntao.platform.common.support.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.support.qiniu.pili.*;
import com.yuntao.platform.common.utils.DateUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class QiNiuTools {

//    private static String QINIU_DOMAIN = "http://res.doublefit.cn/";
    private static String QINIU_DOMAIN;
    private static String QINIU_LIVE_DOMAIN;
    private static String QINIU_DIAN_DOMAIN;
    private static String ACCESS_KEY ;
    private static String SECRET_KEY;
    private static String bucket;
    private static String hubName ;  //直播空间名称i

    private static Auth auth;

    private static Client cli;

    private static Hub hub;


    /**
     * 初始化,配置参数
     * @param initMaps
     */
    public static void init(Map<String,String> initMaps){
        QINIU_DOMAIN = initMaps.get("qiniu.domain");
        QINIU_LIVE_DOMAIN = initMaps.get("qiniu.live.domain");
        QINIU_DIAN_DOMAIN = initMaps.get("qiniu.dian.domain");
        ACCESS_KEY = initMaps.get("qiniu.accessKey");
        SECRET_KEY = initMaps.get("qiniu.secretKey");
        bucket = initMaps.get("qiniu.bucket");
        hubName = initMaps.get("qiniu.hubName");

        auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        //初始化client
        cli = new Client(ACCESS_KEY,SECRET_KEY);

        Config.APIHost = "pili.qiniuapi.com";

        //初始化Hub
        hub = cli.newHub(hubName);

    }


    public static String getToken() {
        String token = auth.uploadToken(bucket,null,DateUtil.DAY_SECONDS,null);
        return token;
    }

    public static String getOverToken(String key) {
        String token = auth.uploadToken(bucket, key,DateUtil.DAY_MILLIS,null);
        return token;
    }


    /**
     * 上传给定名称的文件
     * @param data
     * @param fileName
     * @return
     */
    public static String uploadFileFixName(byte[] data, String fileName) {
        String token = getOverToken(fileName);
        UploadManager uploadManager = new UploadManager();
        try {
            Response res = uploadManager.put(data, fileName, token);
            if (res.isOK()) {
                return QINIU_DOMAIN + fileName;
            }
        } catch (QiniuException e) {
            throw new BizException(e.getMessage(), e);
        }
        return "";
    }


    /**
     * 上传文件
     * @param data
     * @return
     */
    public static String uploadFile(byte data[]) {
        return uploadFile(data, "");
    }


    /**
     * 创建直播
     * @return
     */
    public static LiveObject crateLiveStream(String streamKey){
        //创建流
        Stream stream = null;
        try {
            stream = hub.create(streamKey);
        }catch (PiliException e){
            throw new RuntimeException("create live failed!",e);
        }

        //RTMP推流地址,默认24 * 365 小时
        String publishUrl = cli.RTMPPublishURL("pili-publish."+QINIU_LIVE_DOMAIN, hubName, streamKey, 365 * 24 * 3600);

        //RTMP直播地址
        String playUrl = cli.RTMPPlayURL("pili-live-rtmp."+QINIU_LIVE_DOMAIN, hubName, streamKey);

        //hls直播地址
        String hlsPlayUrl = cli.HLSPlayURL("pili-live-hls."+QINIU_LIVE_DOMAIN, hubName, streamKey);

        //HDL直播地址 (flv)
        String hdlPlayUrl = cli.HDLPlayURL("pili-live-hdl."+QINIU_DIAN_DOMAIN, hubName, streamKey);

        // 截图直播地址
        String snapshotPlayUrl = cli.SnapshotPlayURL("pili-live-snapshot."+QINIU_DIAN_DOMAIN, hubName, streamKey);


        LiveObject liveObject = new LiveObject();
        liveObject.setPublishUrl(publishUrl);
        liveObject.setPlayUrl(playUrl);
        liveObject.setHlsPlayUrl(hlsPlayUrl);
        liveObject.setHdlPlayUrl(hdlPlayUrl);
        liveObject.setSnapshotPlayUrl(snapshotPlayUrl);
        return liveObject;
    }

    /**
     * 保存直播为录播，作为点播用
     * @param streamKey
     */
    public static String saveLive(String streamKey,Date startTime,Date endTime){
        //获得流
        Stream stream = null;
        long start = 0;
        long end = 0;
        if(startTime != null){
            start = startTime.getTime() / 1000;
        }
        if(endTime != null){
            end = endTime.getTime() / 1000;
        }
        try {
            stream = hub.get(streamKey);
            String fileName = stream.save(start, end);
            return "http://"+QINIU_DIAN_DOMAIN+"/"+fileName;
        } catch (PiliException e) {
            throw new BizException("save live failed!",e);
        }
    }

    /**
     * 转码
     * @param streamKey
     */
    public static String converts(String streamKey){
        //获得流
        Stream stream = null;
        try {
            stream = hub.get(streamKey);
            String result = stream.converts();
            return result;
        } catch (PiliException e) {
            throw new BizException("converts live failed!",e);
        }
    }

    /**
     * 直播状态信息
     * @param streamKey
     */
    public static String liveStatus(String streamKey){
        //获得流
        Stream stream = null;
        try {
            stream = hub.get(streamKey);
            String result = stream.liveStatus().toJson();
            return result;
        } catch (PiliException e) {
            throw new BizException("get live status failed!",e);
        }
    }

    /**
     * 直播截图
     * @param streamKey
     */
    public static String snapshot(String streamKey,Date date){
        //获得流
        Stream stream = null;
        try {
            stream = hub.get(streamKey);
            String result = stream.snapshot(date.getTime()/1000);
            JSONObject jsonObject = new JSONObject(result);
            String fname = jsonObject.getString("fname");
            return QINIU_DIAN_DOMAIN+"/"+fname;
        } catch (Exception e) {
            throw new BizException("get live status failed!",e);
        }
    }

    private static String uploadFile(byte[] data, String fileName) {
        String token = getToken();
        UploadManager uploadManager = new UploadManager();
        try {
            String key =
                    DigestUtils.md5Hex(new String(data)).toUpperCase();
            String subffix = getFileSuffix(fileName);
            if (StringUtils.isNotBlank(subffix)) {
                key = key + "." + subffix;
            }
            Response res = uploadManager.put(data, key, token);
            if (res.isOK()) {
                return QINIU_DOMAIN + key;
            }
        } catch (QiniuException e) {
            throw new BizException(e.getMessage(), e);
        }
        return "";
    }
    private static String getFileSuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int index = StringUtils.lastIndexOf(fileName, ".");
        return StringUtils.substring(fileName, index + 1, fileName.length());
    }

    public static void main(String[] args) {
        String time = "2016-05-18 21:12:42";
        Date date = DateUtil.getDateFmtYMDHMS(time);
        long seconds1 = date.getTime()/ 1000;
        System.out.printf("second1="+seconds1);
        long seconds = DateUtils.getFragmentInSeconds(date, Calendar.SECOND);
        System.out.printf("seconds="+seconds);
    }
}
