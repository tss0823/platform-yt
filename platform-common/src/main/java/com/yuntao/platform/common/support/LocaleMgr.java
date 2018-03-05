package com.yuntao.platform.common.support;

import com.yuntao.platform.common.utils.DateUtil;
import com.yuntao.platform.common.filter.ResponseHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by tangshengshan on 17-2-17.
 */
public class LocaleMgr {

    private Locale usLocale = Locale.US;
    private Locale cnLocale = Locale.SIMPLIFIED_CHINESE;
    private Locale twLocale = Locale.TRADITIONAL_CHINESE;
    private ResourceBundle usResourceBundle = ResourceBundle.getBundle("i18n/base",usLocale);
    private ResourceBundle cnResourceBundle = ResourceBundle.getBundle("i18n/base",cnLocale);
    private ResourceBundle twResourceBundle = ResourceBundle.getBundle("i18n/base",twLocale);
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/base",Locale.getDefault());

    public LocaleMgr(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if(request == null){ //没有request环境
            return;
        }
        String localeStr;
        Cookie cookie = WebUtils.getCookie(request, "locale");
        if(cookie == null){
            Locale myLocale = Locale.getDefault();
            localeStr = myLocale.toString();

            //再把locale下发到response cookie中
            HttpServletResponse response = (HttpServletResponse) ResponseHolder.get();
            Cookie sidCookie = new Cookie("locale", localeStr);
            sidCookie.setMaxAge(DateUtil.MONTH_SECONDS);
            sidCookie.setPath("/");
            response.addCookie(sidCookie);
            //end
        }else{
            localeStr = cookie.getValue();
        }

        if(localeStr.toString().equals(usLocale.toString())){  //us
            resourceBundle = usResourceBundle;
        }else if(localeStr.toString().equals(cnLocale.toString())){  //cn
            resourceBundle = cnResourceBundle;
        }else if(localeStr.toString().equals(twLocale.toString())){  //tw
            resourceBundle = twResourceBundle;
        }else{  //没有找到就用us
            resourceBundle = usResourceBundle;
        }
    }

    public String getValue(String key){
        return resourceBundle.getString(key);
    }

    public Map<String, String> getMapData(){
        Map<String,String> dataMap = new HashMap<>();
        Enumeration<String> keys = resourceBundle.getKeys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            dataMap.put(key,getValue(key));
        }
        return dataMap;

    }
}
