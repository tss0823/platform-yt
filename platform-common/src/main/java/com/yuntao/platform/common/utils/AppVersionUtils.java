package com.yuntao.platform.common.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * app版本管理工具类
 * Created by shan on 2017/4/21.
 */
public class AppVersionUtils {

    /**
     * 获取版本号
     * @return
     */
    public static String getAppVersion(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        Map<String, String> userAgentMap = SystemUtil.getUserAgentMap(request.getHeader("user-agent"));
        String appVersion = userAgentMap.get("appVersion");
        return appVersion;
    }

    /**
     * 版本比较
     * @param appVersion
     * @param compareAppVersion
     * @return  0 相同; > 0 大于比较版本; < 0 小于比较的版本
     */
    public static int appVersionCompare(String appVersion,String compareAppVersion){
        if(StringUtils.isEmpty(appVersion)){
            appVersion = "1.0.0";
        }
        String[] firstNumbers = appVersion.split("\\.");
        String[] secondNumbers = compareAppVersion.split("\\.");
        for (int i = 0; i < firstNumbers.length;i++){
            int result = Integer.valueOf(firstNumbers[i]) - Integer.valueOf(secondNumbers[i]);
            if(result != 0){
                return result;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        int result = appVersionCompare("1.2.0", "1.2.0");
        System.out.println(result);
    }
}
