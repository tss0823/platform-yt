package com.yuntao.platform.common.support;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.ListMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeTuiUtils {

    protected final static Logger stackLog = LoggerFactory.getLogger("stackLog");

    //采用"Java SDK 快速入门"， "第二步 获取访问凭证 "中获得的应用配置，用户可以自行替换
    private static String appId = "";
    private static String appKey = "";
    private static String masterSecret = "";

    /**
     * 初始化,配置参数
     *
     * @param initMaps
     */
    public static void init(Map<String, String> initMaps) {
        appId = initMaps.get("getui.appId");
        appKey = initMaps.get("getui.appKey");
        masterSecret = initMaps.get("getui.masterSecret");
    }

    //别名推送方式
    // static String Alias = "";
    static String host = "http://sdk.open.api.igexin.com/apiex.htm";

    private static NotificationTemplate notificationTemplateDemo(String title,String content,String transContent) {
        NotificationTemplate template = new NotificationTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appKey);
        // 设置通知栏标题与内容
        template.setTitle(title);
        template.setText(content);
        // 配置通知栏图标
        template.setLogo("icon.png");
        // 配置通知栏网络图标
        template.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        template.setIsRing(true);
        template.setIsVibrate(true);
        template.setIsClearable(true);
        // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
        template.setTransmissionType(2);
        template.setTransmissionContent(transContent);
        return template;
    }

    public static TransmissionTemplate getTemplate(String title,String content,String transContent) {
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(transContent);
        template.setTransmissionType(2);
        APNPayload payload = new APNPayload();
        //在已有数字基础上加1显示，设置为-1时，在已有数字上减1显示，设置为数字时，显示指定数字
        payload.setAutoBadge("+1");
        payload.setContentAvailable(1);
        payload.setSound("default");
        payload.setCategory("$由客户端定义");


        //简单模式APNPayload.SimpleMsg
        APNPayload.DictionaryAlertMsg dictionaryAlertMsg = new APNPayload.DictionaryAlertMsg();
        dictionaryAlertMsg.setTitle(title);
        dictionaryAlertMsg.setBody(content);


//        payload.setAlertMsg(new APNPayload.SimpleAlertMsg("hello"));
        payload.setAlertMsg(dictionaryAlertMsg);

        //字典模式使用APNPayload.DictionaryAlertMsg
        //payload.setAlertMsg(getDictionaryAlertMsg());

        // 添加多媒体资源
//        payload.addMultiMedia(new MultiMedia().setResType(MultiMedia.MediaType.video)
//                .setResUrl("http://ol5mrj259.bkt.clouddn.com/test2.mp4")
//                .setOnlyWifi(true));

        template.setAPNInfo(payload);
        return template;
    }
    private static APNPayload.DictionaryAlertMsg getDictionaryAlertMsg(){
        APNPayload.DictionaryAlertMsg alertMsg = new APNPayload.DictionaryAlertMsg();
        alertMsg.setBody("body");
        alertMsg.setActionLocKey("ActionLockey");
        alertMsg.setLocKey("LocKey");
        alertMsg.addLocArg("loc-args");
        alertMsg.setLaunchImage("launch-image");
        // iOS8.2以上版本支持
        alertMsg.setTitle("Title");
        alertMsg.setTitleLocKey("TitleLocKey");
        alertMsg.addTitleLocArg("TitleLocArg");
        return alertMsg;
    }


    public static void pushMessage(String clientId, String title, String content,String transContent) {
        List<String> clientIds = new ArrayList<>();
        clientIds.add(clientId);
        pushMessage(clientIds, title, content,transContent);

    }

    public static void pushMessage(List<String> clientIds, String title, String content,String transContent) {

        // 配置返回每个用户返回用户状态，可选
        System.setProperty("gexin.rp.sdk.pushlist.needDetails", "true");
        // 配置返回每个别名及其对应cid的用户状态，可选
        // System.setProperty("gexin_pushList_needAliasDetails", "true");
        IGtPush push = new IGtPush(host, appKey, masterSecret);
        // 通知透传模板
//        NotificationTemplate template = notificationTemplateDemo(title, content,transContent);
        TransmissionTemplate template = getTemplate(title, content, transContent);
//        NotificationTemplate template = (NotificationTemplate) template1
        ListMessage message = new ListMessage();
        message.setData(template);
        // 设置消息离线，并设置离线时间
        message.setOffline(true);
        // 离线有效时间，单位为毫秒，可选
        message.setOfflineExpireTime(24 * 1000 * 3600);
        // 配置推送目标
        List targets = new ArrayList();
        for (String clientId : clientIds) {
            Target target = new Target();
            target.setAppId(appId);
//            String alias = SystemConstant.TuiSong.dfGetuiProd;
//            if (!CustomizedPropertyConfigurer.isProd()) {
//                alias = SystemConstant.TuiSong.dfGetuiTest;
//            }
//            alias += "_" + userId;
            target.setClientId(clientId);
            targets.add(target);
        }
        // taskId用于在推送时去查找对应的message
        String taskId = push.getContentId(message);
        IPushResult ret = null;
        try {
            ret = push.pushMessageToList(taskId, targets);
            stackLog.info("getui info taskId="+taskId+"," + ret.getResponse().toString());
        } catch (RequestException e) {
            stackLog.error("getui res error,taskId="+taskId+",clientIds=" + StringUtils.join(clientIds, ","));
        }
    }

    public static void main(String[] args) throws Exception {
//        pushMessage("4eb9cab011c0accf254d23140fb635ee", "测试推送by shan", "我的测试内容22222","{\"aaaa\":\"1111\"}");
        pushMessage("4eb9cab011c0accf254d23140fb635ee", "测试推送by shan", "我的测试内容22222","{\"title\":\"测试标题111\"，\"content\":\"测试内容\"}");
        pushMessage("b8638dbc5b9e5693058145f74ab8990a", "测试推送by shan", "我的测试内容22222","{\"title\":\"测试标题111\"，\"content\":\"测试内容\"}");


    }
}
