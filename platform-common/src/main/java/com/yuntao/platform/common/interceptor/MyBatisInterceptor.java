package com.yuntao.platform.common.interceptor;

import com.yuntao.platform.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.text.DateFormat;
import java.util.*;

@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MyBatisInterceptor implements Interceptor {

    private Logger log = LoggerFactory.getLogger(getClass());

    private final static Logger stackLog = org.slf4j.LoggerFactory.getLogger("stackLog");
    private final static Logger taskLog = org.slf4j.LoggerFactory.getLogger("taskLog");

    private static Log performanceLog = LogFactory.getLog("performanceLog");

    @SuppressWarnings("unused")
    private Properties properties;

    @SuppressWarnings("unused")
    private ApplicationContext applicationContext;

    public MyBatisInterceptor(ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
//        this.myBatisInterceptorCache = (JedisUtil) applicationContext.getBean("myBatisInterceptorCache");
    }

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        String mapper = StringUtils.substringBeforeLast(sqlId, ".");
        if (StringUtils.isBlank(mapper)) {
            return invocation.proceed();
        }

        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        Object returnValue = null;
        Configuration configuration = mappedStatement.getConfiguration();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Object parameterObject = boundSql.getParameterObject();
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        String sql = boundSql.getSql();
        sql = sql.replaceAll("[\\s]+", " ");
        if (sqlCommandType == SqlCommandType.SELECT) {
            if (metaObject.hasGetter("id")) {
                Long id = (Long) metaObject.getValue("id");
                if (id != null && id instanceof Long) {
//                    returnValue = myBatisInterceptorCache.getObj(mapper + "_id_" + id);
//                    if (returnValue != null && shouldCache(returnValue)) {
//                        List<ResultMap> resultMaps= mappedStatement.getResultMaps();
//                        if(resultMaps!=null && resultMaps.size()==1 && BaseEntityImpl.class.isAssignableFrom(resultMaps.get(0).getType())){
//                            log.debug("命中缓存:" + sqlId + ":id=" + id);
//                            return returnValue;
//                        }
//                    }
                }
            }
        }


        //性能处理
        long start = System.currentTimeMillis();
        returnValue = invocation.proceed();

        //业务处理
        try {
            long end = System.currentTimeMillis();
            long takeTime = (end - start);
            if (takeTime > 20) {
                StringBuilder str = new StringBuilder();
                str.append(sqlId);
                str.append(":");
                str.append(sql);
                str.append(" take ");
                str.append(takeTime);
                str.append(" ms");
                performanceLog.info(str.toString());
            }
            //end

            if (returnValue != null) {
//            if (sqlCommandType == SqlCommandType.SELECT && shouldCache(returnValue)) {
//                if (metaObject.hasGetter("id")) {
//                    Long id = (Long) metaObject.getValue("id");
//                    if (id != null && id instanceof Long) {
//                        myBatisInterceptorCache.setObj(mapper + "_id_" + id, DateUtil.HOUR_SECONDS, returnValue);
//                        log.debug("设置缓存:" + sqlId + ":id=" + id);
//                    }
//                }
            } else if (sqlCommandType == SqlCommandType.UPDATE) {
//                if (metaObject.hasGetter("id")) {
//                    Long id = (Long) metaObject.getValue("id");
//                    if (id != null && id instanceof Long) {
//                        myBatisInterceptorCache.delete(mapper + "_id_" + id);
//                        log.debug("清空缓存:" + sqlId + ":id=" + id);
//                    }
//                }
//            }
            }

            //sql 日志处理
            StringBuilder sqlSb = new StringBuilder("^SQL^");
            sqlSb.append(sqlId);
            sqlSb.append("^#^");
            sqlSb.append(sqlCommandType.name());
            sqlSb.append("^#^");
            String filledSql = showSql(sqlCommandType.name(), configuration, boundSql);
            sqlSb.append(filledSql);
            sqlSb.append("^#^");
            if(returnValue instanceof ArrayList && ((ArrayList) returnValue).size() > 0){
                int updateSize = ((ArrayList) returnValue).size();
                sqlSb.append(((ArrayList) returnValue).get(0).toString()+"#"+updateSize);
            }else{
                sqlSb.append(returnValue);
            }
            sqlSb.append("^#^");
            String returnJson = JsonUtils.object2Json(returnValue);
            sqlSb.append(returnJson);
            stackLog.info(sqlSb.toString());
            taskLog.info(sqlSb.toString());

            //end

        } catch (Exception e) {
            log.error("mybatis interceptor error", e);
        }
        return returnValue;
    }

//    @SuppressWarnings("rawtypes")
//    private boolean shouldCache(Object object){
//        return object != null && object instanceof List && ((List) object).size() == 1 && ((List) object).get(0) instanceof BaseEntityImpl;
//    }


    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }

    public static String showSql(String sqlType, Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        String value = getParameterValue(obj);
                        if (StringUtils.equalsIgnoreCase(sqlType, SqlCommandType.INSERT.name())) {
                            value = propertyName + "#" + value;
                        }
                        sql = sql.replaceFirst("\\?", value); //注释掉，可能报错 todo
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        String value = getParameterValue(obj);
                        if (StringUtils.equalsIgnoreCase(sqlType, SqlCommandType.INSERT.name())) {
                            value = propertyName + "#" + value;
                        }
                        sql = sql.replaceFirst("\\?", value);
                    }
                }
            }
        }
        return sql;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties0) {
        this.properties = properties0;
    }

}
