package com.yuntao.platform.common.rpc;

import com.alibaba.dubbo.rpc.*;
import com.yuntao.platform.common.log.HbLogBean;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.log.LogContext;
import com.yuntao.platform.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shengshan.tang on 2016/8/30.
 */
public class YtAfterFilter implements Filter {


    protected Logger stackLog = LoggerFactory.getLogger("stackLog");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //before
        String logContextJson = invocation.getAttachment("logContext");

        if (StringUtils.isNotEmpty(logContextJson)) {
            LogContext logContext = JsonUtils.json2Object(logContextJson,LogContext.class);
            if(logContext != null){
                HbLogContextMgr.setLogContext(logContext);
            }
        }
        Result invoke = invoker.invoke(invocation);
        //after
        return invoke;
    }
}
