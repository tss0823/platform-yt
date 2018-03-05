package com.yuntao.platform.common.support.QQGzh;

import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.http.HttpNewUtils;
import com.yuntao.platform.common.http.RequestRes;
import com.yuntao.platform.common.http.ResponseRes;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by siben.yang on 2017/7/28.
 */
public class QQGzhUtils {

    protected final static Logger stackLog = LoggerFactory.getLogger("stackLog");

    private static String appKey = "";//a4e0f582102fb45cfea6d736be120229
    private static String appid = "";//101421816

    /**
     * 初始化,配置参数
     *
     * @param initMaps
     */
    public static void init(Map<String, String> initMaps) {
        appKey = initMaps.get("qq.gzh.appKey");
        appid = initMaps.get("qq.gzh.appid");
    }




    public static QQAccessToken getAccessToken(String code, String redirectUri){
        String url = "https://graph.qq.com/oauth2.0/token?client_id="+appid+"&client_secret="+appKey+"&code="+code
                +"&grant_type=authorization_code&redirect_uri="+redirectUri;
        String str = "";
        try{
            URI uri = new URI(url);
            SimpleClientHttpRequestFactory schr = new SimpleClientHttpRequestFactory();
            ClientHttpRequest chr = schr.createRequest(uri, HttpMethod.GET);
//            chr.getHeaders().add("Content-Type","application/json");
            ClientHttpResponse res = chr.execute();
            InputStream is = res.getBody(); //获得返回数据,注意这里是个流
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            QQAccessToken qqAccessToken = new QQAccessToken();
            while((str = br.readLine())!=null){
                System.out.println("返回内容" + str);//获得页面内容或返回内容
                String[] StrigStr = str.split("&");
                String text = "";
                for(int i = 0; i < StrigStr.length; i++){
                    text = StrigStr[i];
                    if(text.contains("access_token")){
                        qqAccessToken.setAccessToken(text.substring(text.indexOf("=")+1));
                    }
                    if(text.contains("expires_in")){
                        qqAccessToken.setExpires_in(text.substring(text.indexOf("=")+1));
                    }
                    if(text.contains("refresh_token")){
                        qqAccessToken.setRefreshToken(text.substring(text.indexOf("=")+1));
                    }
                }
            }
            return qqAccessToken;
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException("analyse json failed! json getAccessToken=" + url + "url" + str,e);
        }
    }

    public static QQAccessToken getOpenId(String accessToken){
        String url = "https://graph.qq.com/oauth2.0/me?access_token="+accessToken;
        String str = "";
        try{
            URI uri = new URI(url);
            SimpleClientHttpRequestFactory schr = new SimpleClientHttpRequestFactory();
            ClientHttpRequest chr = schr.createRequest(uri, HttpMethod.GET);
            chr.getHeaders().add("Content-Type","application/json");
            ClientHttpResponse res = chr.execute();
            InputStream is = res.getBody(); //获得返回数据,注意这里是个流
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            QQAccessToken qqAccessToken = new QQAccessToken();
            while((str = br.readLine())!=null){
                str = str.substring(str.indexOf("{"),str.indexOf("}")+1);
                JSONObject jsonObject = new JSONObject(str);
                qqAccessToken.setOpenid(jsonObject.getString("openid"));
                qqAccessToken.setClientId(jsonObject.getString("client_id"));
            }
            qqAccessToken.setAccessToken(accessToken);
            return qqAccessToken;
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException("analyse json failed! json= getOpenId" + url + "url" + str,e);
        }
    }

    public static QQUserInfo getUserInfo(String accessToken, String openid){
        String url = "https://graph.qq.com/user/get_user_info?access_token="+accessToken+
                "&oauth_consumer_key="+appid+"&openid="+openid;
        String str = "";
        try {
            URI uri = new URI(url);
            SimpleClientHttpRequestFactory schr = new SimpleClientHttpRequestFactory();
            ClientHttpRequest chr = schr.createRequest(uri, HttpMethod.GET);
            chr.getHeaders().add("Content-Type","application/json");
            ClientHttpResponse res = chr.execute();
            InputStream is = res.getBody(); //获得返回数据,注意这里是个流
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            QQAccessToken qqAccessToken = new QQAccessToken();
            String readLine;
            while((readLine = br.readLine())!=null){
                str += readLine;
            }
            System.out.println(str);
            JSONObject jsonObject = new JSONObject(str);
//            qqAccessToken.setOpenid(jsonObject.getString("openid"));
//            qqAccessToken.setClientId(jsonObject.getString("client_id"));
//            JSONObject jsonObject = new JSONObject(bodyText);
            String nickname = jsonObject.getString("nickname");
            String avatar = jsonObject.getString("figureurl");
            String sex = jsonObject.getString("gender");
            String year = jsonObject.getString("year");
            String province = jsonObject.getString("province");
            String city = jsonObject.getString("city");
            QQUserInfo qqUserInfo = new QQUserInfo();
            qqUserInfo.setNickname(nickname);
            qqUserInfo.setAvatar(avatar);
            qqUserInfo.setSex(sex);
            qqUserInfo.setyear(year);
            qqUserInfo.setProvince(province);
            qqUserInfo.setCity(city);
            return qqUserInfo;
        } catch (Exception e) {
            throw new BizException("analyse json failed! json= getUserInfo" + str ,e);
        }
    }

}
