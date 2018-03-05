package com.yuntao.platform.common.cache;

/**
 * Created by shan on 2016/3/25.
 */
public interface CacheService {

    /**
     * 设置对象，默认过期时间为永久
     * scope app
     * @param key
     * @param value
     */
    void set(String key,Object value);

    void set(String key, Object value,int period);

    /**
     * 设置对象
     * scope all app
     * @param key
     * @param value
     */
    void setGlobal(String key,Object value);

    void setGlobal(String key, Object value,int period);

    Object get(String key);

    Object getGlobal(String key);

    void remove(String key);

    void removeGlobal(String key);

}
