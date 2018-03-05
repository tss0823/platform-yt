package com.yuntao.platform.common.auth;

/**
 *
 * 授权用户接口
 * Created by tangshengshan on 16-12-19.
 */
public interface AuthUserService {

    AuthUser getAuthUser();

    AuthUser getAuthUser(String sid);

}
