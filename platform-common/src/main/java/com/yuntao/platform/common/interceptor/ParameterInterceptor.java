package com.yuntao.platform.common.interceptor;

import com.yuntao.platform.common.log.HbLogContextMgr;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ParameterInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ((handler instanceof HandlerMethod) == false) {
            return true;
        }
        HbLogContextMgr.appendParameter(request);
        return super.preHandle(request, response, handler);
    }
}
