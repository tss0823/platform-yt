package com.yuntao.platform.common.rpc;

import com.alibaba.dubbo.rpc.*;
import com.yuntao.platform.common.log.HbLogContextMgr;
import com.yuntao.platform.common.log.LogContext;
import com.yuntao.platform.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shengshan.tang on 2016/8/30.
 */
public class YtBeforeFilter implements Filter {

    protected Logger stackLog = LoggerFactory.getLogger("stackLog");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //before
        LogContext logContext = HbLogContextMgr.getLogContext();
        if(logContext != null){
//            ((RpcInvocation) invocation).setAttachment("logContext", JsonUtils.object2Json(logContext));
            RpcContext.getContext().setAttachment("logContext",JsonUtils.object2Json(logContext));
        }
        long startTime = System.currentTimeMillis();
        stackLog.info("call rpc before start");
        stackLog.info(invoker.toString());
        stackLog.info(invocation.toString());
        Result invoke = invoker.invoke(invocation);
        stackLog.info("call rpc before end,takeTime="+(System.currentTimeMillis()-startTime)+" ms");
        return invoke;
    }
}
