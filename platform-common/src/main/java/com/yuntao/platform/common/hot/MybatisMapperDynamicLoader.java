package com.yuntao.platform.common.hot;

import com.yuntao.platform.common.exception.BizException;
import com.yuntao.platform.common.utils.AppConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class MybatisMapperDynamicLoader /**implements  ApplicationContextAware **/ {

//    private final HashMap<String, String> mappers = new HashMap<String, String>();
//    private volatile ConfigurableApplicationContext context = null;
//    private volatile Scanner scanner = null;

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.context = (ConfigurableApplicationContext) applicationContext;
//    }

    private static Scanner scanner = new Scanner();

    public static void reload(){
        if (!StringUtils.equals(AppConfigUtils.getModel(),"dev")) {  //开发模式下起作用
            return;
        }
        try {
            scanner.reloadXML();
        } catch (Exception e) {
            throw new BizException("重新加载 mapper.xml 失败",e);
        }

    }


//    @Override
//    public void afterPropertiesSet() throws Exception {
//        try {
//            scanner = new Scanner();
//            new Timer(true).schedule(new TimerTask() {
//                public void run() {
//                    try {
//                        if (scanner.isChanged()) {
//                            System.out.println("load mapper.xml");
//                            scanner.reloadXML();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 10 * 1000, 5 * 1000);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//    }

    @SuppressWarnings("unchecked")
    static class Scanner {
        private static final String XML_RESOURCE_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/*Mapper.xml";
        private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        public Scanner() {
//            Resource[] resources = findResource();
//            if (resources != null) {
//                for (Resource resource : resources) {
//                    String key = resource.getURI().toString();
//                    String value = getMd(resource);
//                    mappers.put(key, value);
//                }
//            }
        }
        public void reloadXML() throws Exception {
//            SqlSessionFactory factory = context.getBean(SqlSessionFactory.class);
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            SqlSessionFactory factory = wac.getBean(SqlSessionFactory.class);
            Configuration configuration = factory.getConfiguration();
            removeConfig(configuration);
            for (Resource resource : findResource()) {
                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(), configuration, resource.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        }
        private void removeConfig(Configuration configuration) throws Exception {
            Class<?> classConfig = configuration.getClass();
            clearMap(classConfig, configuration, "mappedStatements");
            clearMap(classConfig, configuration, "caches");
            clearMap(classConfig, configuration, "resultMaps");
            clearMap(classConfig, configuration, "parameterMaps");
            clearMap(classConfig, configuration, "keyGenerators");
            clearMap(classConfig, configuration, "sqlFragments");
            clearSet(classConfig, configuration, "loadedResources");
        }
        private void clearMap(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
            Field field = classConfig.getDeclaredField(fieldName);
            field.setAccessible(true);
            ((Map) field.get(configuration)).clear();
        }
        private void clearSet(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
            Field field = classConfig.getDeclaredField(fieldName);
            field.setAccessible(true);
            ((Set) field.get(configuration)).clear();
        }
//        public boolean isChanged() throws IOException {
//            boolean isChanged = false;
//            for (Resource resource : findResource()) {
//                String key = resource.getURI().toString();
//                String value = getMd(resource);
//                if (!value.equals(mappers.get(key))) {
//                    isChanged = true;
//                    mappers.put(key, value);
//                }
//            }
//            return isChanged;
//        }
        private Resource[] findResource()  {
            try {
                return resourcePatternResolver.getResources(XML_RESOURCE_PATTERN);
            } catch (IOException e) {
                throw new BizException("mapper 文件获取我失败",e);
            }
        }
        private String getMd(Resource resource) throws IOException {
            return new StringBuilder().append(resource.contentLength()).append("-").append(resource.lastModified()).toString();
        }
    }
}