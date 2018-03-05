package com.yuntao.platform.common.hot;

import com.yuntao.platform.common.CustomizedPropertyConfigurer;
import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.utils.AppConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.Properties;

public class PropertiesDynamicLoader /**implements  ApplicationContextAware **/ {


    public static void reload(){
        if (!StringUtils.equals(AppConfigUtils.getModel(),"dev")) {  //开发模式下起作用
            return;
        }
        try {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            CustomizedPropertyConfigurer customizedPropertyConfigurer = wac.getBean(CustomizedPropertyConfigurer.class);
            customizedPropertyConfigurer.init(new Properties());
        } catch (Exception e) {
            throw new BizException("重新加载 config-xxx.properties 失败",e);
        }

    }

}