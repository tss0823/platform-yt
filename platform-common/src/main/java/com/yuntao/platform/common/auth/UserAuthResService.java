package com.yuntao.platform.common.auth;

import java.util.Set;

/**
 * 授权用户资源接口
 * Created by tangshengshan on 16-12-19.
 */
public interface UserAuthResService {

    /**
     * 获取用户的授权链接资源
      * @param userId
     * @return
     */
    Set<String> selectAuthUrlsByUserId(Long userId);

    /**
     * 链接是否需要检测校验
     * @param url
     * @return
     */
    boolean needCheck(String url);
}
