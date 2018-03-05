package com.yuntao.platform.common;

import com.yuntao.platform.common.support.GeTuiUtils;
import com.yuntao.platform.common.support.QQGzh.QQGzhUtils;
import com.yuntao.platform.common.support.SendMsgUtils;
import com.yuntao.platform.common.support.weixinGzh.WeiXinGzhUtils;
import com.yuntao.platform.common.support.qiniu.QiNiuTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.InputStream;
import java.util.*;

public class CustomizedPropertyConfigurer extends PropertyPlaceholderConfigurer {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private static final Map<String, String> ctxPropertiesMap = new HashMap<String, String>();

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        try {
            super.processProperties(beanFactory, props);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.init(props);
    }

    public void init(Properties props){
        loadFromParam(props);
        loadFromSystemProp();
        loadFromFile();
        loadFromCommonFile();
        printProp();

        //初始化依赖变量
        QiNiuTools.init(ctxPropertiesMap);
        SendMsgUtils.init(ctxPropertiesMap);
        GeTuiUtils.init(ctxPropertiesMap);
        WeiXinGzhUtils.init(ctxPropertiesMap);
        QQGzhUtils.init(ctxPropertiesMap);

    }

    private void printProp() {
        Set<Map.Entry<String, String>> entrySet = ctxPropertiesMap.entrySet();
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        while (it != null && it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.containsIgnoreCase(key, "password")) {
                value = "******";
            }
            log.warn("find property " + key + "=" + value);
        }
    }


    private void loadFromFile() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env/config-" + ctxPropertiesMap.get("spring.profiles.active") + ".properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
            Set<String> stringPropertyNames = p.stringPropertyNames();
            for (String name : stringPropertyNames) {
                String value = p.getProperty(name);
                putProp(name, value);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadFromCommonFile() {
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config-common.properties");
            Properties p = new Properties();
            p.load(inputStream);
            Set<String> stringPropertyNames = p.stringPropertyNames();
            for (String name : stringPropertyNames) {
                String value = p.getProperty(name);
                putProp(name, value);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadFromSystemProp() {
        Properties properties = System.getProperties();
        Set<String> propertyNames = properties.stringPropertyNames();
        if (propertyNames != null) {
            Iterator<String> itPN = propertyNames.iterator();
            while (itPN != null && itPN.hasNext()) {
                String key = itPN.next();
                putProp(key, properties.get(key) + "");
            }
        }
    }

    private void loadFromParam(Properties props) {
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            putProp(keyStr, value);
        }
    }

    private void putProp(String key, String value) {
        ctxPropertiesMap.put(key, value);
    }

    public static String getContextProperty(String name) {
        return ctxPropertiesMap.get(name);
    }

    public static String getCpKey() {
        return getContextProperty("cpKey");
    }
    public static String getModel() {
        return getContextProperty("spring.profiles.active");
    }
    public static String getAppName() {
        return getContextProperty("appName");
    }
    public static boolean isProd() {
        return StringUtils.equals(getContextProperty("spring.profiles.active") + "", "prod");
    }

    public static boolean isTest() {
        return StringUtils.equals(getContextProperty("spring.profiles.active") + "", "test");
    }
    public static boolean isDev() {
        return StringUtils.equals(getContextProperty("spring.profiles.active") + "", "dev");
    }

}