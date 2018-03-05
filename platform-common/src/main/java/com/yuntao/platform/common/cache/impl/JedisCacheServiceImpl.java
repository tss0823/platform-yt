package com.yuntao.platform.common.cache.impl;

import com.yuntao.platform.common.cache.CacheService;
import com.yuntao.platform.common.cache.JedisExecuteHandler;
import com.yuntao.platform.common.cache.JedisService;
import com.yuntao.platform.common.cache.QueueService;
import com.yuntao.platform.common.utils.AppConfigUtils;
import com.yuntao.platform.common.utils.JsonUtils;
import com.yuntao.platform.common.utils.SerializeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shan on 2016/5/5.
 */
@Service("cacheService")
public class JedisCacheServiceImpl implements CacheService,QueueService,JedisService {

    @Value("${redis.info}")
    private String redisInfo;

    @Value("${redis.namespace}")
    private String namespace;

    private final static Logger stackLog = org.slf4j.LoggerFactory.getLogger("stackLog");
    private final static Logger taskLog = org.slf4j.LoggerFactory.getLogger("taskLog");

    private final static Logger log = org.slf4j.LoggerFactory.getLogger(JedisCacheServiceImpl.class);

    private ShardedJedisPool shardedJedisPool;

    @PostConstruct
    public void init() {
        List<JedisShardInfo> shards = new ArrayList<>();
        String[] redisInfoArray = redisInfo.split(",");
        for (String redisInfo : redisInfoArray) {
            if (StringUtils.isEmpty(redisInfo)) {
                continue;
            }
            String redisHost = redisInfo.split(":")[0];
            int redisPort = Integer.valueOf(redisInfo.split(":")[1]);
            String pwd = redisInfo.split(":")[2];
            JedisShardInfo jedisShardInfo = new JedisShardInfo(redisHost, redisPort);
            jedisShardInfo.setPassword(pwd);
            shards.add(jedisShardInfo);
        }
        JedisPoolConfig config = new JedisPoolConfig();
        //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
        //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
        config.setMaxTotal(500);
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(5);
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(1000 * 5);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);
        shardedJedisPool = new ShardedJedisPool(new JedisPoolConfig(), shards);
    }

    private void setCommon(String key, Object value,Integer period,boolean isGlobal) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            byte[] bs = SerializeUtil.serialize(value);
//            int period = 60 * 60 * 5;  //默认过期时间
            if(!isGlobal){  //不是全部,把appName加上
                key = AppConfigUtils.getAppName() + "_"+namespace+"_"+key;
            }else{
                key = namespace+"_"+key;
            }
            if(period != null){
                jedis.setex(key.getBytes(),period,bs);
            }else {
                jedis.set(key.getBytes(),bs);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^set to cache,isGlobal="+isGlobal+",period="+period+",key="+key+",value="+value+"^#^"+ JsonUtils.object2Json(value));
            taskLog.info("^CACHE^set to cache,isGlobal="+isGlobal+",period="+period+",key="+key+",value="+value+"^#^"+ JsonUtils.object2Json(value));
        }

    }

    @Override
    public void set(String key, Object value) {
        setCommon(key,value,null,false);
    }

    @Override
    public void set(String key, Object value, int period) {
        setCommon(key,value,period,false);
    }

    @Override
    public void setGlobal(String key, Object value) {
        setCommon(key,value,null,true);

    }

    @Override
    public void setGlobal(String key, Object value, int period) {
        setCommon(key,value,period,true);
    }


    private Object getCommon(String key,boolean isGlobal) {
        ShardedJedis jedis = null;
        Object value = null;
        try{
            jedis = shardedJedisPool.getResource();
            if(!isGlobal){  //不是全部,把appName加上
                key = AppConfigUtils.getAppName() + "_"+namespace+"_"+key;
            }else{
                key = namespace+"_"+key;
            }
            byte [] bs = jedis.get(key.getBytes());
            value = SerializeUtil.unserialize(bs);
            return value;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^get from cache,isGlobal="+isGlobal+",key="+key+",value="+value+"^#^"+ JsonUtils.object2Json(value));
            taskLog.info("^CACHE^get from cache,isGlobal="+isGlobal+",key="+key+",value="+value+"^#^"+ JsonUtils.object2Json(value));
        }
    }

    @Override
    public Object get(String key) {
        return getCommon(key,false);
    }

    @Override
    public Object getGlobal(String key) {
        return getCommon(key,true);
    }

    @Override
    public void remove(String key) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = AppConfigUtils.getAppName() + "_"+namespace+"_"+key;
            jedis.del(key.getBytes());
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^remove from cache,key="+key);
            taskLog.info("^CACHE^remove from cache,key="+key);
        }

    }

    @Override
    public void removeGlobal(String key) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            jedis.del(key.getBytes());
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^removeGlobal from cache,key="+key);
            taskLog.info("^CACHE^removeGlobal from cache,key="+key);
        }

    }

    @Override
    public void add(String key, String msg) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            jedis.lpush(key,msg);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
        }

    }

    @Override
    public String pop(String key) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            return jedis.rpop(key);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
        }
    }

    @Override
    public void ltrim(String key, int start, int end) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            jedis.ltrim(key,start,end);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^ltrim cache,key="+key+",start="+start+",end="+end);
            taskLog.info("^CACHE^ltrim cache,key="+key+",start="+start+",end="+end);
            log.info("^CACHE^ltrim cache,key="+key+",start="+start+",end="+end);
        }

    }

    @Override
    public List<String> lrange(String key, int start, int end) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            return jedis.lrange(key,start,end);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^lrange cache,key="+key+",start="+start+",end="+end);
            taskLog.info("^CACHE^lrange cache,key="+key+",start="+start+",end="+end);
            log.info("^CACHE^lrange cache,key="+key+",start="+start+",end="+end);
        }
    }

    @Override
    public long length(String key) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            return jedis.llen(key);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^llen cache,key="+key);
            taskLog.info("^CACHE^llen cache,key="+key);
            log.info("^CACHE^llen cache,key="+key);
        }
    }


    @Override
    public void execute(String key, JedisExecuteHandler jedisExecuteHandler) {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            key = namespace+"_"+key;
            jedisExecuteHandler.execute(jedis, key);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            shardedJedisPool.returnResource(jedis);
            stackLog.info("^CACHE^execute cache,key="+key);
            taskLog.info("^CACHE^execute cache,key="+key);
            log.info("^CACHE^execute cache,key="+key);
        }
    }


    @Override
    public String getKey(String key) {
        key = namespace+"_"+key;
        return key;
    }

    @Override
    public ShardedJedis getShardedJedis() {
        ShardedJedis jedis = null;
        try{
            jedis = shardedJedisPool.getResource();
            return jedis;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
//            shardedJedisPool.returnResource(jedis);
        }
    }

    @Override
    public void returnResource(ShardedJedis jedis) {
        shardedJedisPool.returnResource(jedis);
    }
}
