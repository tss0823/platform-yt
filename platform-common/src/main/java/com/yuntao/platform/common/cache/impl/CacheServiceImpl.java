package com.yuntao.platform.common.cache.impl;

import com.yuntao.platform.common.cache.CacheService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by shan on 2016/3/25.
 */
@Service("cacheLocalService")
public class CacheServiceImpl implements CacheService {

    int cacheSize = 800;

    //cache 使用LRU 策略
    Map<String,Object> cacheMap = new LinkedHashMap(cacheSize){
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() >= cacheSize;
        }
    };

    {
        cacheMap = Collections.synchronizedMap(cacheMap);
    }

    @Override
    public void set(String key, Object value) {
        cacheMap.put(key,value);

    }


    @Override
    public void set(String key, Object value, int period) {
        throw new RuntimeException("not implement");
    }

    @Override
    public void setGlobal(String key, Object value) {
        throw new RuntimeException("not implement");
    }

    @Override
    public void setGlobal(String key, Object value, int period) {
        throw new RuntimeException("not implement");
    }

    @Override
    public Object get(String key) {
        return cacheMap.get(key);
    }

    @Override
    public Object getGlobal(String key) {
        throw new RuntimeException("not implement");
    }

    @Override
    public void remove(String key) {
        cacheMap.remove(key);
    }

    @Override
    public void removeGlobal(String key) {
        throw new RuntimeException("not implement");
    }
}
