package com.yuntao.platform.common.support.weixinGzh;

import com.mchange.lang.ByteUtils;
import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.http.HttpNewUtils;
import com.yuntao.platform.common.http.RequestRes;
import com.yuntao.platform.common.http.ResponseRes;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shan on 2017/6/28.
 */
public class WeiXinGzhUtils {

    protected final static Logger stackLog = LoggerFactory.getLogger("stackLog");

    private static String appKey = "";
    private static String appSecret = "";

    /**
     * 初始化,配置参数
     *
     * @param initMaps
     */
    public static void init(Map<String, String> initMaps) {
        appKey = initMaps.get("weixin.gzh.appKey");
        appSecret = initMaps.get("weixin.gzh.appSecret");
    }


    public static WeixinAccessToken getAccessToken(String code){
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appKey+"&secret="+appSecret+"&code="+code+"&grant_type=authorization_code";
        ResponseRes responseRes = HttpNewUtils.get(url);

        String bodyText = responseRes.getBodyText();
        try {
            JSONObject jsonObject = new JSONObject(bodyText);
            String accessToken = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");
            WeixinAccessToken weixinAccessToken = new WeixinAccessToken();
            weixinAccessToken.setAccessToken(accessToken);
            weixinAccessToken.setOpenid(openid);
            return weixinAccessToken;
        } catch (JSONException e) {
            throw new BizException("analyse json failed! json="+bodyText,e);
        }
    }

    public static WeixinUserInfo getUserInfo(String accessToken,String openid){
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token="+accessToken+"&openid="+openid+"&lang=zh_CN";
        ResponseRes responseRes = HttpNewUtils.get(url);
        String bodyText = responseRes.getBodyText();
        try {
            JSONObject jsonObject = new JSONObject(bodyText);
            String nickname = jsonObject.getString("nickname");
            String avatar = jsonObject.getString("headimgurl");
            Integer sex = jsonObject.getInt("sex");
            String country = jsonObject.getString("country");
            String province = jsonObject.getString("province");
            String city = jsonObject.getString("city");
            WeixinUserInfo weixinUserInfo = new WeixinUserInfo();
            weixinUserInfo.setNickname(nickname);
            weixinUserInfo.setAvatar(avatar);
            weixinUserInfo.setSex(sex);
            weixinUserInfo.setCountry(country);
            weixinUserInfo.setProvince(province);
            weixinUserInfo.setCity(city);
            return weixinUserInfo;
        } catch (JSONException e) {
            throw new BizException("analyse json failed! json="+bodyText,e);
        }
    }

    public static WeixinAccessToken getToken(){
        String url = "https://api.weixin.qq.com/cgi-bin/token?appid="+appKey+"&secret="+appSecret+"&grant_type=client_credential";
//        Map<String,String> paramMap = new HashMap<>();
//        paramMap.put("grant_type", "client_credential");
//        paramMap.put("appid", appKey);
//        paramMap.put("secret", appSecret);

        ResponseRes responseRes = HttpNewUtils.get(url);
        String bodyText = responseRes.getBodyText();
        try {
            JSONObject jsonObject = new JSONObject(bodyText);
            String accessToken = jsonObject.getString("access_token");
            WeixinAccessToken weixinAccessToken = new WeixinAccessToken();
            weixinAccessToken.setAccessToken(accessToken);
            return weixinAccessToken;
        } catch (JSONException e) {
            throw new BizException("analyse token json failed! json="+bodyText,e);
        }
    }

    public static String ticket(String accessToken) {
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
        ResponseRes responseRes = HttpNewUtils.get(url);
        String bodyText = responseRes.getBodyText();
        try {
            JSONObject jsonObject = new JSONObject(bodyText);
            String ticket = jsonObject.getString("ticket");
            return ticket;
        } catch (JSONException e) {
            throw new BizException("analyse ticket json failed! json="+bodyText,e);
        }


    }




    public static String signConfigJS(String ticket,String url, Long timestamp, String nonceStr) {
        StringBuffer sb = new StringBuffer();
        sb.append("jsapi_ticket=" + ticket);
        sb.append("&noncestr=" + nonceStr);
        sb.append("&timestamp=" + timestamp);
        sb.append("&url=" + url);
//        Logger.error("sign:%s", sb);
        String str = sb.toString();
        stackLog.info("签名前数据="+str);
        String sign = DigestUtils.shaHex(str);
//        String sign = sha1(str);
//        String sign = sha1Online(str);
        stackLog.info("签名后数据 online2="+sign);

        String weiXinSign = DigestUtils.shaHex("jsapi_ticket=sM4AOVdWfPE4DxkXGEs8VMCPGGVi4C3VM0P37wVUCFvkVAy_90u5h9nbSlYy3-Sl-HhTdfl2fzFy1AOcHKP7qg&noncestr=Wm3WZYTPz0wzccnW&timestamp=1414587457&url=http://mp.weixin.qq.com?params=value");
        stackLog.info("微信签名后数据="+weiXinSign);
        try {
            String sha = ByteUtils.toHexAscii(MessageDigest.getInstance("sha").digest(str.getBytes()));
            stackLog.info("toHexAscii 方法签名后数据="+sha);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sign;

    }

    public static String sha1(String str){
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("sha1加密出错");
        }

    }
    public static String sha1Online(String str){
        try {
            RequestRes requestRes = new RequestRes();
            requestRes.setUrl("http://tool.geekapp.cn/encrypt_do.php");
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("type_hash","sha1");
            paramMap.put("str",str);
            requestRes.setParams(paramMap);
            ResponseRes responseRes = HttpNewUtils.execute(requestRes);
            String bodyText = responseRes.getBodyText();
            JSONObject jsonObject = new JSONObject(bodyText);
            String value = jsonObject.getString("lower_case");
            return value;

        } catch (Exception e) {
            throw new BizException("sha1加密出错");
        }

    }
    public static void main(String[] args) {
        String str = "jsapi_ticket=HoagFKDcsGMVCIY2vOjf9vAS739zbydUYDtxG-0LPhv7_LFFtxHzwxL2N35ZZfbeTa-WEiy4NHsNaLJCeQ_Hpg&noncestr=dMhNTr7N62JYShp3hydPa2C8f7TT45jM×tamp=1501747117&url=http://h5.chinamons.com/share/takeActive.html?shareId=12&openid=1&isTake=false&model=prod&app=true";
        String sign = DigestUtils.shaHex(str);
        System.out.println(sign);


    }
}
