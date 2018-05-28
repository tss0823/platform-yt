package com.yuntao.platform.common.auth;

import com.yuntao.platform.common.cache.CacheService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验
 * Created by shan on 2017/8/22.
 */
@Component
public class AuthCheckMgr {


    @Autowired
    private CacheService cacheService;

    @Autowired(required = false)
    private UserAuthResService userAuthResService;


    public  boolean checkAuth(AuthUser user,String url){
        if (!userAuthResService.needCheck(url)) {  //无需校验
            return true;
        }
        Set<String> authUrls = userAuthResService.selectAuthUrlsByUserId(user.getUserId());
        if(CollectionUtils.isNotEmpty(authUrls)){
            return authUrls.contains(url);
        }
        return false;
    }
}
