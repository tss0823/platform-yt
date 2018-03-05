package com.yuntao.platform.common.auth;

import com.yuntao.platform.common.annotation.Auth;
import com.yuntao.platform.common.annotation.NeedLogin;
import com.yuntao.platform.common.constant.SystemConstant;
import com.yuntao.platform.common.exception.AuthException;
import com.yuntao.platform.common.utils.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 登录校验
 * Created by shan on 2017/8/22.
 */
@Component
public class LoginCheckMgr {

    @Autowired
    private AuthUserService userService;

    public  AuthUser checkLogin(Method method){
        NeedLogin needLogin = method.getAnnotation(NeedLogin.class);
        AuthUser user = null;
        if (needLogin != null) {  //需要权限校验
            user = userService.getAuthUser();
            if (user == null) {
                AuthException exception = new AuthException("您已退出登录，请重新登录", SystemConstant.ResponseCode.NOT_LOGIN);
                throw exception;
            }

            //需要绑定权限 TODO
            Auth auth = method.getAnnotation(Auth.class);
            if (auth != null && auth.needBind()) {
                if (NumberUtil.getNumber(user.getBindStatus()).equals(0)) {  //未绑定
                    AuthException exception = new AuthException("您需要先绑定手机号才能操作", SystemConstant.ResponseCode.NOT_BIND);
                    throw exception;
                }
            }
        }
        return user;
    }
}
