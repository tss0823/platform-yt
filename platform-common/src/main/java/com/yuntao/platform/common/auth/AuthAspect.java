package com.yuntao.platform.common.auth;

import com.yuntao.platform.common.constant.SystemConstant;
import com.yuntao.platform.common.exception.AuthException;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.profiler.ProfileTaskManger;
import com.yuntao.platform.common.utils.AppConfigUtils;
import com.yuntao.platform.common.web.DocObject;
import com.yuntao.platform.common.web.ResponseObject;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;


@Aspect
@Component
public class AuthAspect {

    private final static Logger stackLog = LoggerFactory.getLogger("stackLog");

    @Autowired
    private LoginCheckMgr loginCheckMgr;

    @Autowired
    private AuthUserService userService;

    @Autowired
    private AuthCheckMgr authCheckMgr;

    @Value("${auth.check}")
    private boolean authCheck;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object aroundController(final ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //start profile
        if(!method.getName().equals("checkServerStatus")){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String clsName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            String actionMsg = clsName + "." + methodName;
            ProfileTaskManger.startFirst(actionMsg, request.getRequestURL().toString());
        }
        //end
        return authExecute(joinPoint);
    }

    private Object authExecute(final ProceedingJoinPoint joinPoint)  throws Throwable{
       //登录用户校验
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuthUser user = loginCheckMgr.checkLogin(method);
        //end

        //资源权限校验
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String appName = AppConfigUtils.getAppName();
        String requestURI = request.getRequestURI();
        if(authCheck && user!= null && StringUtils.equals(appName,"bos") && !StringUtils.startsWith(requestURI,"/manager/")
                && !StringUtils.startsWith(requestURI,"/trans/")){  //针对bos 校验
            if (!authCheckMgr.checkAuth(user, requestURI)) {
                AuthException exception = new AuthException("您没有权限操作", SystemConstant.ResponseCode.NOT_AUTHORITY);
                throw exception;
            }
        }
        //end

        //请求字段校验处理
        String genDoc = request.getParameter(SystemConstant.GEN_DOC);
        boolean isGenDoc = BooleanUtils.toBoolean(genDoc);
        DocObject docObject = ParamValidateCheckMgr.processReqParam(joinPoint, request,isGenDoc);
        //end

        if(user != null){
            HbLogContextMgr.setUser(user.getUserId(), user.getMobile(), user.getUserName());
        }
        Object returnObj = joinPoint.proceed();
        HbLogContextMgr.setResponse(returnObj);

        //返回数据处理
        if (isGenDoc && returnObj instanceof ResponseObject) {
            ResponseObject responseObject = (ResponseObject) returnObj;
//            Object data = responseObject.getData();  //这里是多级
            Map<String, Object> returnMap = new LinkedMap();
            ParamValidateCheckMgr.processResData(responseObject,returnMap);
            docObject.setReturnObj(returnMap);
            responseObject.setData(docObject);
//            docObject.setReturnMemoObj(returnObj);
        }
        //end

        if(user == null){
            Object sid = request.getAttribute(SystemConstant.USER_TOKEN);
            if(sid != null){ //避免重复设置sid
                user = userService.getAuthUser(sid.toString());
            }else{
                user = userService.getAuthUser();
            }
        }
        if(user != null){
            HbLogContextMgr.setUser(user.getUserId(), user.getMobile(), user.getUserName());
        }
        return returnObj;
    }
}
