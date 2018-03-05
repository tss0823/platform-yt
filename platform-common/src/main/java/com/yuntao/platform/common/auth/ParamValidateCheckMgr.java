package com.yuntao.platform.common.auth;

import com.yuntao.platform.common.annotation.*;
import com.yuntao.platform.common.utils.AppConfigUtils;
import com.yuntao.platform.common.utils.ExceptionUtils;
import com.yuntao.platform.common.web.DocObject;
import com.yuntao.platform.common.web.DocReqFieldObject;
import com.yuntao.platform.common.web.DocResFieldObject;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 参数合法校验,文档规格处理
 * Created by shan on 2017/8/22.
 */
public class ParamValidateCheckMgr {

    private final static Logger stackLog = LoggerFactory.getLogger("stackLog");

    public static DocObject processReqParam(ProceedingJoinPoint joinPoint, HttpServletRequest request,Boolean isGenDoc) {
        DocObject docObject = new DocObject();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (isGenDoc) {
            ReqMethodComment reqMethodComment = method.getAnnotation(ReqMethodComment.class);
            if (reqMethodComment != null) {
                docObject.setComment(reqMethodComment.value());
                docObject.setUrl(request.getRequestURI());
                docObject.setAuthor(reqMethodComment.author());
                docObject.setVer(reqMethodComment.ver());
                docObject.setModule(reqMethodComment.module());
                String appName = AppConfigUtils.getValue("appName");
//                if(StringUtils.equalsIgnoreCase(appName,"member")){
//                    appName = "user";
//                }
                docObject.setAppName(appName);
            }
        }
        Map<String, DocReqFieldObject> paramMap = new LinkedMap();
        docObject.setParamObj(paramMap);

        //获取参数
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Annotation[][] parameterAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterAnnotations();
        if(parameterNames == null){
            return docObject;
        }
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            if (parameterAnnotation == null || parameterAnnotation.length == 0) {
                stackLog.info("请求参数[" + parameterName + "]注解为空");
                continue;
            }
            Annotation annotation = parameterAnnotation[0];
            Object arg = args[i];
            if (annotation instanceof ParamFieldComment || annotation instanceof ModelFieldComment) { //原始类型
                String comment = null;
                boolean required = false;
                if(annotation instanceof  ParamFieldComment){
                    ParamFieldComment paramFieldComment = (ParamFieldComment) annotation;
                    comment = paramFieldComment.value();
                    required = paramFieldComment.required();
                }else{
                    ModelFieldComment paramFieldComment = (ModelFieldComment) annotation;
                    comment = paramFieldComment.value();
                    required = paramFieldComment.required();
                }

                //validate
                if (required && arg == null) {
                    throw new IllegalArgumentException(comment + "[" + parameterName + "]不能为空");
                }
                //TODO 扩展regex
                //end
                if (isGenDoc) {
                    DocReqFieldObject docReqFieldObject = new DocReqFieldObject();
                    docReqFieldObject.setCode(parameterName);
                    if(arg != null){
                        docReqFieldObject.setValue(arg.toString());
                    }
                    docReqFieldObject.setComment(comment);
                    docReqFieldObject.setValidateText(required ? "必填" : "可选");
                    paramMap.put(parameterName, docReqFieldObject);
                }
            } else if (annotation instanceof ParamClassComment && arg != null) {  //对象
//                Map<String,Object> childParamMap = new LinkedMap();
//                paramMap.put(parameterName,childParamMap);
                Field[] declaredFields = FieldUtils.getAllFields(arg.getClass());
                for (Field declaredField : declaredFields) {  //参数约定为二级
                    String name = declaredField.getName();
                    ParamFieldComment paramFieldComment = declaredField.getAnnotation(ParamFieldComment.class);
                    ModelFieldComment modelFieldComment = declaredField.getAnnotation(ModelFieldComment.class);
                    if (paramFieldComment == null && modelFieldComment == null) { //没有注解,忽略该参数
                        continue;
                    }
                    boolean required = paramFieldComment.required();
                    String comment = paramFieldComment.value();
                    Class<?> type = declaredField.getType();
                    String methodName = StringUtils.capitalize(name);
                    if (type.getSimpleName().equals("java.lang.Boolean") || type.getSimpleName().equals("boolean")) {
                        methodName = "is" + methodName;
                    } else {
                        methodName = "get" + methodName;
                    }
                    Object value = null;
                    try {
                        value = MethodUtils.invokeMethod(arg, methodName);
                    } catch (Exception e) {
                        stackLog.info(ExceptionUtils.getPrintStackTrace(e));
                    }
                    //validate
                    if (required && value == null) {
                        throw new IllegalArgumentException(comment + "[" + name + "]不能为空");
                    }
                    //TODO 扩展regex
                    //end
                    if (isGenDoc) {
                        DocReqFieldObject docReqFieldObject = new DocReqFieldObject();
                        docReqFieldObject.setCode(name);
                        if (value != null) {//array TODO
                            docReqFieldObject.setValue(value.toString());
                        }
                        docReqFieldObject.setComment(comment);
                        docReqFieldObject.setValidateText(required ? "必填" : "可选");
                        paramMap.put(name, docReqFieldObject);

                    }
                }
            }
        }
        return  docObject;
    }

    public static Integer processResData(Object data, Map<String, Object> returnMap) {
        if(data == null){
            return 0;
        }
//        Field[] declaredFields = data.getClass().getDeclaredFields();
        List<Field> allFieldsList = FieldUtils.getAllFieldsList(data.getClass());
        int execFieldSize = 0;
        for (Field declaredField : allFieldsList) {
            String name = declaredField.getName();
            Class<?> type = declaredField.getType();
            String methodName = StringUtils.capitalize(name);
            if (type.getSimpleName().equals("java.lang.Boolean") || type.getSimpleName().equals("boolean")) {
                methodName = "is" + methodName;
            } else {
                methodName = "get" + methodName;
            }
            Method method = MethodUtils.getMatchingAccessibleMethod(data.getClass(), methodName);
//            Object value = FieldUtils.readField(declaredField, data);
            if (method == null) {
                continue;
            }
            Object value = null;
            try {
                value = MethodUtils.invokeMethod(data, methodName);
            } catch (Exception e) {
                stackLog.info(ExceptionUtils.getPrintStackTrace(e));
            }
            execFieldSize++;
//            Object value = null;
//            try{
//            }catch (Exception e){
//            }
            DocResFieldObject resFieldObject = new DocResFieldObject();
            resFieldObject.setCode(name);
            if (value != null) {
                resFieldObject.setValue(value);
            }
            String comment = null;
            boolean isClass = false;
            ReturnFieldComment returnFieldComment = declaredField.getAnnotation(ReturnFieldComment.class);
            if(returnFieldComment != null){
                comment = returnFieldComment.value();
            }else{
                ModelFieldComment modelFieldComment = declaredField.getAnnotation(ModelFieldComment.class);
                if(modelFieldComment != null){
                    comment = modelFieldComment.value();
                }
            }
            if(comment == null) {
                ReturnClassComment returnClassComment = declaredField.getAnnotation(ReturnClassComment.class);
                if (returnClassComment != null) {
                    isClass = true;
                    comment = returnClassComment.value();
                } else {   //没有注解,就不返回
                    continue;
                }
            }
            if (comment != null) {
                resFieldObject.setComment(comment);
                if (isClass) {
                    name = name + "[" + comment + "]";
                    if (value != null && value.getClass().getSimpleName().equals("ArrayList")) {
                        List<Object> arrayReturnList = new ArrayList<>();
                        returnMap.put(name, arrayReturnList);
//                        if (CollectionUtils.isEmpty(arrayReturnList)) {
//                            continue;
//                        }
                        setReturnDocForList(value, arrayReturnList);
                    } else {
                        Map<String, Object> childReturnMap = new LinkedMap();
                        returnMap.put(name, childReturnMap);

//                        String valueSimpleName = value.getClass().getSimpleName();
//                        if(valueSimpleName.equals("String") || valueSimpleName.equals("Integer"))
                        int execFieldSize2 = processResData(value, childReturnMap);
                        if(value != null && execFieldSize2 == 0){
                            returnMap.put(name,value);
                        }
//                        configReturnDoc(value, childReturnMap);
                    }
                } else {
                    Object returnValue = resFieldObject.getValue();
                    if (returnValue != null && returnValue instanceof Date) {
                        returnValue = ((Date) returnValue).getTime();
                    }
                    returnMap.put(name, returnValue + "^#^" + resFieldObject.getComment());
                }
            }

        }
        return execFieldSize;
    }

    private static void setReturnDocForList(Object dataList, List<Object> arrayReturnList) {
//        Field[] declaredFields = data.getClass().getDeclaredFields();
        List arrayDataList = (List) dataList;
        if (arrayDataList.size() == 0) {
            return;
        }
        Class<?> aClass = arrayDataList.get(0).getClass();
        for (Object data : arrayDataList) {
            Map<String, Object> returnMap = new LinkedMap();
            arrayReturnList.add(returnMap);
            List<Field> allFieldsList = FieldUtils.getAllFieldsList(aClass);
            for (Field declaredField : allFieldsList) {
                String name = declaredField.getName();
                Class<?> type = declaredField.getType();
                String methodName = StringUtils.capitalize(name);
                if (type.getSimpleName().equals("java.lang.Boolean") || type.getSimpleName().equals("boolean")) {
                    methodName = "is" + methodName;
                } else {
                    methodName = "get" + methodName;
                }
                Method method = MethodUtils.getMatchingAccessibleMethod(data.getClass(), methodName);
//            Object value = FieldUtils.readField(declaredField, data);
                if (method == null) {
                    continue;
                }
                Object value = null;
                try {
                    value = MethodUtils.invokeMethod(data, methodName);
                } catch (Exception e) {
                    stackLog.info(ExceptionUtils.getPrintStackTrace(e));
                }
                DocResFieldObject resFieldObject = new DocResFieldObject();
                resFieldObject.setCode(name);
                if (value != null) {
                    resFieldObject.setValue(value);
                }
                String comment = null;
                boolean isClass = false;
                ReturnFieldComment returnFieldComment = declaredField.getAnnotation(ReturnFieldComment.class);
                if (returnFieldComment != null) {
                    comment = returnFieldComment.value();
                } else {
                    ModelFieldComment modelFieldComment = declaredField.getAnnotation(ModelFieldComment.class);
                    if (modelFieldComment != null) {
                        comment = modelFieldComment.value();
                    }
                }
                if (comment == null) {
                    ReturnClassComment returnClassComment = declaredField.getAnnotation(ReturnClassComment.class);
                    if (returnClassComment != null) {
                        isClass = true;
                        comment = returnClassComment.value();
                    } else {   //没有注解,就不返回
                        continue;
                    }
                }
                if (comment != null) {
                    resFieldObject.setComment(comment);
                    if (isClass) {
//                        Map<String,Object> childReturnMap = new LinkedMap();
//                        returnMap.put(name,childReturnMap);
//                        configReturnDoc(value,childReturnMap);
                        name = name + "[" + comment + "]";
                        if (value != null && value.getClass().getSimpleName().equals("ArrayList")) {
                            List<Object> arrayChildReturnList = new ArrayList<>();
                            returnMap.put(name, arrayChildReturnList);
                            setReturnDocForList(value, arrayChildReturnList);
                        } else {
                            Map<String, Object> childReturnMap = new LinkedMap();
                            returnMap.put(name, childReturnMap);
                            int execFieldSize = processResData(value, childReturnMap);
                            if (value != null && execFieldSize == 0) {
                                returnMap.put(name, value);
                            }
                        }
                    } else {
                        Object returnValue = resFieldObject.getValue();
                        if (returnValue != null && returnValue instanceof Date) {
                            returnValue = ((Date) returnValue).getTime();
                        }
                        returnMap.put(name, returnValue + "^#^" + resFieldObject.getComment());
                    }
                }

            }


        }

    }
}
