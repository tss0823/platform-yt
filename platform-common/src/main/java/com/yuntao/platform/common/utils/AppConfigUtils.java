package com.yuntao.platform.common.utils;

import com.yuntao.platform.common.CustomizedPropertyConfigurer;
import org.springframework.core.env.PropertySources;

import java.util.Properties;

/**
 * Created by shan on 2016/8/6.
 */
public class AppConfigUtils {
    private static Properties properties;
    public static void init(PropertySources propertySources){
        properties = (Properties) propertySources.get("localProperties").getSource();

    }
    public static String getValue(String name){
        return CustomizedPropertyConfigurer.getContextProperty(name);
    }
    public static String getModel(){
        return CustomizedPropertyConfigurer.getModel();
    }
    public static boolean isTest(){
        return CustomizedPropertyConfigurer.getModel().equals("test");
    }
    public static boolean isProd(){
        return CustomizedPropertyConfigurer.getModel().equals("prod");
    }
    public static String getAppName(){
        return CustomizedPropertyConfigurer.getAppName();
    }
}
