package com.yuntao.platform.common.filter;

import com.yuntao.platform.common.http.HttpNewUtils;
import com.yuntao.platform.common.http.RequestRes;
import com.yuntao.platform.common.http.ResponseRes;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.profiler.ProfileTaskManger;
import com.yuntao.platform.common.utils.AppConfigUtils;
import com.yuntao.platform.common.utils.JsonUtils;
import com.yuntao.platform.common.web.ResponseObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResponseHolderFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(ResponseHolderFilter.class);

    /**
     * 缺省监测值为100毫秒，超过这个值的request请求将被记录
     */
    private int threshold = 100;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        log.info("res filter init...");
        ServletContext sc = filterConfig.getServletContext();
        WebApplicationContext beanFactory = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

        //初始化config-xx.properties系统配置
//        PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer = (PropertySourcesPlaceholderConfigurer) beanFactory.getBean("propertyPlaceholderConfigurer");
//        AppConfigUtils.init(propertyPlaceholderConfigurer.getAppliedPropertySources());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
//        log.info("brefore request ...");
        //
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String url = request.getRequestURI();
        response.setHeader("Access-Control-Allow-Origin", "*");
        //end
        HbLogContextMgr.startStack(request);
        try {
            ResponseHolder.set(response);
//            chain.doFilter(request, response);
            //开始mock data
            boolean isReturnMock = false;
            if (!AppConfigUtils.isProd() && !url.endsWith("checkServerStatus")) {
                String getMockDataUrl = AppConfigUtils.getValue("mockDataUrl");
                RequestRes requestRes = new RequestRes();
                requestRes.setUrl(getMockDataUrl);
                Map<String,String> paramMap = new HashMap<>();
                requestRes.setParams(paramMap);
                paramMap.put("appName",AppConfigUtils.getAppName());
                paramMap.put("pathUrl",url);

                try{
                    ResponseRes responseRes = HttpNewUtils.execute(requestRes);
                    String bodyText = responseRes.getBodyText();
                    ResponseObject responseObject = JsonUtils.json2Object(bodyText, ResponseObject.class);
                    if (responseObject.isSuccess()) { //成功
                        Object data = responseObject.getData();
                        response.addHeader("Content-type","application/json;charset=UTF-8");
                        response.getWriter().write(data.toString());
                        isReturnMock = true;
                    }
                }catch (Exception e){
                }
            }
            if(!isReturnMock){
                chain.doFilter(req,resp);
            }
            //end
        } finally {
            ResponseHolder.clear();
            HbLogContextMgr.endStack( request,  response);

            if(!url.endsWith("checkServerStatus")){
                ProfileTaskManger.endLast(threshold);
                ProfileTaskManger.clear();
            }
//            log.info("reponse clear1 ...");
        }
    }

    @Override
    public void destroy() {
//        log.error("reponse clear2 ...");
        ResponseHolder.clear();
    }

    private class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private byte[] body;

        public MyHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            try {
                body = IOUtils.toByteArray(request.getInputStream());
            } catch (IOException ex) {
                body = new byte[0];
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener listener) {

                }

                ByteArrayInputStream bais = new ByteArrayInputStream(body);

                @Override
                public int read() throws IOException {
                    return bais.read();
                }
            };
        }
    }
}
