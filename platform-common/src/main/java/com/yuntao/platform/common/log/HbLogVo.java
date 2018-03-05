package com.yuntao.platform.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shengshan.tang on 9/23/2015 at 3:40 PM
 */
public class HbLogVo implements Serializable {

    public static class SQL{
        private String sql;

        private List<String> parameters;

        private int result;

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public void setParameters(List<String> parameters) {
            this.parameters = parameters;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        @Override
        public String toString() {
            if(CollectionUtils.isEmpty(parameters)){
                return sql;
            }
            if(sql.indexOf("?") == -1){
                return sql;
            }
            for(String param : parameters){
                int startIndex = param.lastIndexOf("(");
                if(startIndex == -1){
                    continue;
                }
                int endIndex = param.indexOf(")",startIndex);
                String dataType = param.substring(startIndex+1,endIndex);
                param = param.substring(0,startIndex);
                if(dataType.equals("String")){
                    param  = "'"+param+"'";
                }
                sql = sql.replaceFirst("\\?",param);
            }
            return sql;
        }
    }

    public static class Req{
        String clientIp;
        String reqUrl;
        Map<String,String> headers;
        Map<String,String> parameters;

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }

        public String getReqUrl() {
            return reqUrl;
        }

        public void setReqUrl(String reqUrl) {
            this.reqUrl = reqUrl;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public String getParamterList(){
            StringBuilder sb = new StringBuilder();
            if(MapUtils.isNotEmpty(parameters)){
                Set<Map.Entry<String,String>> paramSet = parameters.entrySet();
                for(Map.Entry<String,String> entry : paramSet){
                    sb.append("<strong style='color:red'>");
                    sb.append(entry.getKey());
                    sb.append("</strong>");
                    sb.append(" ");
                    sb.append(entry.getValue());
                    sb.append("<br/>");
                }
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<strong style='color:red'>");
            sb.append("clientIp =>");
            sb.append("</strong>");
            sb.append(clientIp);
            sb.append("<br/>");
            sb.append("<strong style='color:red'>");
            sb.append("reqUrl =>");
            sb.append("</strong>");
            sb.append(reqUrl);
            sb.append("<br/>");
            sb.append("<br/>");
            sb.append("<br/>");
            if(MapUtils.isNotEmpty(headers)){
                Set<Map.Entry<String,String>> headerSet = headers.entrySet();
                for(Map.Entry<String,String> entry : headerSet){
                    sb.append("<strong style='color:red'>");
                    sb.append(entry.getKey());
                    sb.append("</strong>");
                    sb.append(" ==>");
                    sb.append(entry.getValue());
                    sb.append("<br/>");
                }
            }
            return sb.toString();
        }
    }

    public static class Resp{
        String result;
        Map<String,String> headers;

        public String getResult() {
            return result;
        }

        public String getFormatResult(){
            if(StringUtils.isEmpty(result)){
                return result;
            }
            //json 格式化
            ObjectMapper mapper = new ObjectMapper();
            try {
                Object json = mapper.readValue(result, Object.class);
                result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            } catch (IOException e) {
            }
            //end
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if(MapUtils.isNotEmpty(headers)){
                Set<Map.Entry<String,String>> headerSet = headers.entrySet();
                for(Map.Entry<String,String> entry : headerSet){
                    sb.append("<strong style='color:red'>");
                    sb.append(entry.getKey());
                    sb.append("</strong>");
                    sb.append(" ==>");
                    sb.append(entry.getValue());
                    sb.append("<br/>");
                }
            }
            return sb.toString();
        }
    }

    private List<SQL> sqls;

    private Req req;

    private Resp resp;

    public List<SQL> getSqls() {
        return sqls;
    }

    public void setSqls(List<SQL> sqls) {
        this.sqls = sqls;
    }

    public Req getReq() {
        return req;
    }

    public void setReq(Req req) {
        this.req = req;
    }

    public Resp getResp() {
        return resp;
    }

    public void setResp(Resp resp) {
        this.resp = resp;
    }
}
