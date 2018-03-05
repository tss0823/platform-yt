package com.yuntao.platform.common.cache;

import java.util.List;

/**
 * Created by shan on 2016/8/19.
 */
public interface QueueService {

    /**
     * left add
     * @param key
     * @param msg
     */
    void add(String key,String msg);

    /**
     * left pop
     * @param key
     * @return
     */
    String pop(String key);

    /**
     * http://www.yiibai.com/redis/lists_ltrim.html
     * 修剪到特定的范围
     * @param key
     * @param start 开始
     * @param end 结束(包含)
     */
    void ltrim(String key,int start,int end);

    /**
     * http://www.yiibai.com/redis/lists_ltrim.html
     * 修剪到特定的范围
     * @param key
     * @param start 开始
     * @param end 结束(包含)
     */
    List<String> lrange(String key, int start, int end);

    /**
     * 长度
     * @param key
     * @return
     */
    long length(String key);



}
