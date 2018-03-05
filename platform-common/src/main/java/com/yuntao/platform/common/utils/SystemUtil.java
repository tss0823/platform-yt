package com.yuntao.platform.common.utils;

import org.apache.commons.lang3.SystemUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by shengshan.tang on 9/21/2015 at 9:59 AM
 */
public class SystemUtil {

    public static String getIp() {
        return getSystemIP( "eth1" );
    }
    public static String getLocalIp() {
        return getSystemIP( "eth0" );
    }

    /**
     * 默认外网地址
     * netType : 网络类型：内网、外网
     */
    private static String getSystemIP( String netType )  {
        try{
            //非linux平台ip
            if(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                return InetAddress.getLocalHost()==null?"":InetAddress.getLocalHost().getHostAddress().toString();
            }
            //linux 平台返回ip
            Enumeration netInterfaces= NetworkInterface.getNetworkInterfaces();
            while(netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                String network = ni.getName();
                if (netType.equalsIgnoreCase( network )) {
                    Enumeration<InetAddress> inetAddres= ni.getInetAddresses();
                    while( inetAddres.hasMoreElements() ) {
                        InetAddress ip = inetAddres.nextElement();
                        if( ip!=null && ip instanceof Inet4Address) {
                            Inet4Address ipv4 = (Inet4Address)ip;
                            return ipv4.getHostAddress();
                        }else if(ip!=null && ip instanceof Inet6Address) {
                            Inet6Address ipv6 = (Inet6Address)ip;
                        }
                    }
                }
            }
        }catch(Exception e){
            return "00";  //返回错误处理ip
        }
        return "01";  //返回没有结果ip
    }

    public static Map<String,String> getUserAgentMap(String ua){
//        String[] strings = ua.split(",");
//        Map<String,String> dataMap = new HashMap() ;
//        for (String str : strings) {
//            String[] keyValues = str.split("/");
//            String key = keyValues[0];
//            String value = keyValues[1];
//            dataMap.put(key,value);
//        }
        Map<String,String> dataMap = (LinkedHashMap<String, String>) JsonUtils.json2Object(ua, Map.class);
        if(dataMap == null){
            dataMap = new HashMap<>();
        }
        return  dataMap;

    }
    public static String getUserAgentValueByKey(String ua,String key){
        Map<String, String> userAgentMap = getUserAgentMap(ua);
        return userAgentMap.get(key);
    }


    public static void main(String[] args) {
        String json = "{\"appName\":\"member\",\"appVersion\":\"1.0.0\"}";
        Map<String,String> map = (LinkedHashMap<String, String>) JsonUtils.json2Object(json, Map.class);
        System.out.printf(map.get("appName"));

//        String ua = "appName/member,appVersion/1.0,systemName/iOS,systemVersion/10.2,deviceVersion/iPhone9,1,width/750,height/1334";
//        Map<String, String> userAgentMap = getUserAgentMap(ua);
//        System.out.printf("userAgentMap="+userAgentMap);
    }
}
